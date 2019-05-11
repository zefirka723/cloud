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
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("server_storage/" + clientPath + "/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + clientPath + "/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }
            // загрузка файла в облако
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + clientPath + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                refreshFilesList(ctx);
            }
            // команды
            if (msg instanceof Command) {
                switch (((Command) msg).getMsgType()) {
                    case "AUTH_REQUEST":
                       clientPath = authService.getPathByLoginAndPassword(((Command) msg).getMsgText());
                       ctx.writeAndFlush(new Command(clientPath == null ? "AUTH_DENIED" : "AUTH_OK"));
                       if(clientPath != null) { refreshFilesList(ctx);  }
                    break;

                    case "REFRESH_REQUEST":
                        refreshFilesList(ctx);
                        break;

                    case "DELETE_REQUEST":
                        if (Files.exists(Paths.get(clientPath + ((Command) msg).getMsgText()))) {
                            Files.delete(Paths.get(clientPath + ((Command) msg).getMsgText()));
                            refreshFilesList(ctx);
                            break;
                        }
                        System.out.println("Удаляемый файл не найден");
                        break;

                    case "DISCONNECT":
                        ctx.writeAndFlush(new Command("DISCONNECTED"));
                }
            }

            // авторизация
//            if (msg instanceof AuthRequest) {
//                if (authService.checkAuthByLoginAndPassword(((AuthRequest) msg).getLogin(), ((AuthRequest) msg).getPassword())) {
//                    clientPath = "server_storage/" + ((AuthRequest) msg).getLogin();
//                    AuthResponse authResponse = new AuthResponse(true, "success!"); //TODO это из ответа сервиса заполнять
//                    ctx.writeAndFlush(authResponse);
//                }
//            }

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
        ctx.writeAndFlush(new Command("REFRESH_FILES_LIST"));
    }

}