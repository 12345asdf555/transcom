package com.yang.serialport.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class Client  
{  
  private EventLoopGroup loop = new NioEventLoopGroup();
  private String ip;
  private String fitemid; 
  public Bootstrap bootstrap = new Bootstrap();
  public MainFrame mainFrame;
  public ConnectionListener CL = new ConnectionListener(this);
  
  public Client(MainFrame mainFrame) {
	// TODO Auto-generated constructor stub
	  this.mainFrame = mainFrame;
  }

  public Client() {
	// TODO Auto-generated constructor stub
  }

  public static void main( String[] args )  
  {  
    new Client().run();  
  }  
  
  public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop) {  
    if (bootstrap != null) {  
      final TcpClientHandler handler = new TcpClientHandler(this);  
      
      try {
		  FileInputStream in = new FileInputStream("IPconfig.txt");  
          InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
          BufferedReader bufReader = new BufferedReader(inReader);  
          String line = null; 
          int writetime=0;
			
		    while((line = bufReader.readLine()) != null){ 
		    	if(writetime==0){
	                ip=line;
	                writetime++;
		    	}
		    	else{
		    		fitemid=line;
		    		writetime=0;
		    	}
          }  

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
      bootstrap.group(eventLoop);  
      bootstrap.channel(NioSocketChannel.class);  
      bootstrap.option(ChannelOption.SO_KEEPALIVE, true);  
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {  
        @Override  
        protected void initChannel(SocketChannel socketChannel) throws Exception { 
          socketChannel.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));    
      	  socketChannel.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));    
      	  socketChannel.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));    
      	  socketChannel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8)); 
          socketChannel.pipeline().addLast(handler);  
          CL.socketChannel = socketChannel;
        }  
      });  
      bootstrap.remoteAddress(ip, 5555);
      bootstrap.connect().addListener(CL); 
    }  
    return bootstrap;  
  }  
  public void run() {  
    createBootstrap(bootstrap, loop);
  }  
}
