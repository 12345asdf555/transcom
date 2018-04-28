package com.yang.serialport.ui;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ConnectionListener implements ChannelFutureListener {
	private Client client;  
	public SocketChannel socketChannel;
	public ConnectionListener(Client client) {  
	    this.client = client;  
	}  
	@Override
	public void operationComplete(ChannelFuture channelFuture) throws Exception {
		// TODO Auto-generated method stub
		if (!channelFuture.isSuccess()) {  
		      //System.out.println("Reconnect");  
		      final EventLoop loop = channelFuture.channel().eventLoop();  
		      loop.schedule(new Runnable() {  
		        @Override  
		        public void run() {  
		          client.createBootstrap(new Bootstrap(), loop);  
		        }  
		      }, 1L, TimeUnit.SECONDS);  
		    }else{
				  client.mainFrame.SocketCli = socketChannel;
				  System.out.println(socketChannel);
			}  
		  }  
	}

