package com.cloud.server;

import com.cloud.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class MainHandler extends ChannelInboundHandlerAdapter {

    String clientPath;
    AuthService authService = new AuthService();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            // запрос на скачивание файла из облака
            if (msg instanceof FileRequest) {
                System.out.println("папка:" + clientPath);
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(clientPath + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(clientPath + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }

            // загрузка файла в облако
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(clientPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                refreshFilesList(ctx);
            }

            // команды
            if (msg instanceof Command) {
                switch (((Command) msg).getCommandType()) {
                    case AUTH_REQUEST:
                        clientPath = "server_storage/" + authService.getPathByLoginAndPassword(((Command) msg).getMsgText()) + "/";
                        ctx.writeAndFlush(new Command(
                                clientPath == null ? Command.CommandType.AUTH_FAILED : Command.CommandType.AUTH_OK));
                        if (clientPath != null) {
                            refreshFilesList(ctx);
                        }
                        break;

                    case CLOUD_FILES_REQUEST:
                        refreshFilesList(ctx);
                        break;

                    case DEL_FILE_REQUEST:
                        if (Files.exists(Paths.get(clientPath + ((Command) msg).getMsgText()))) {
                            Files.delete(Paths.get(clientPath + ((Command) msg).getMsgText()));
                            refreshFilesList(ctx);
                            break;
                        }
                        ctx.writeAndFlush(new Command(Command.CommandType.DEL_FAILED));
                        break;

                    case DISCONNECT:
                        clientPath = null;
                        ctx.writeAndFlush(new Command(Command.CommandType.DISCONNECTED));
                        ctx.close(); //TODO check this out
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


    // обновление списка файлов
    private void refreshFilesList(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new Command(Command.CommandType.CLOUD_FILES_LIST));
    }
}