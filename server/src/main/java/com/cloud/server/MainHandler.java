package com.cloud.server;

import com.cloud.common.AuthRequest;
import com.cloud.common.AuthResponse;
import com.cloud.common.FileMessage;
import com.cloud.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class MainHandler extends ChannelInboundHandlerAdapter {

    String clientPath;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }

            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(clientPath + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(clientPath + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }
            if (msg instanceof FileMessage) {  // если пришёл файл
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(clientPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof AuthRequest) {
                AuthService authService = new AuthService();
                if (authService.checkAuthByLoginAndPassword(((AuthRequest) msg).getLogin(), ((AuthRequest) msg).getPassword())) {
                    clientPath = "server_storage/" + ((AuthRequest) msg).getLogin();
                    AuthResponse authResponse = new AuthResponse(true, "success!"); //TODO это из ответа сервиса заполнять
                    ctx.writeAndFlush(authResponse);
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
}