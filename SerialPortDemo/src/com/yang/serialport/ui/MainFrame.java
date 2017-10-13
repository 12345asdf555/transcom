/*
 * MainFrame.java
 *
 * Created on 2016.8.19
 */

package com.yang.serialport.ui;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.yang.serialport.exception.NoSuchPort;
import com.yang.serialport.exception.NotASerialPort;
import com.yang.serialport.exception.PortInUse;
import com.yang.serialport.exception.ReadDataFromSerialPortFailure;
import com.yang.serialport.exception.SendDataToSerialPortFailure;
import com.yang.serialport.exception.SerialPortInputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortOutputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortParameterFailure;
import com.yang.serialport.exception.TooManyListeners;
import com.yang.serialport.manage.SerialPortManager;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

import org.sqlite.*;

import com.yang.serialport.ui.*;

public class MainFrame extends JFrame {
	 
	Connection c = null;
    Statement stmt = null;
    // 输出流对象
    OutputStream outputStream;
    // Socket变量
    private Socket socket=null;
    private Socket websocketlink=null;
    private ServerSocket serverSocket = null;

	/**
	 * 程序界面宽度
	 */
	public static final int WIDTH = 500;

	/**
	 * 程序界面高度
	 */
	public static final int HEIGHT = 360;

	private JTextArea dataView = new JTextArea();
	private JScrollPane scrollDataView = new JScrollPane(dataView);

	// 串口设置面板
	private JPanel serialPortPanel = new JPanel();
	private JLabel serialPortLabel = new JLabel("串口");
	private JLabel baudrateLabel = new JLabel("波特率");
	private JComboBox commChoice = new JComboBox();
	private JComboBox baudrateChoice = new JComboBox();

	// 操作面板
	private JPanel operatePanel = new JPanel();
	private JTextField dataInput = new JTextField();
	private JButton serialPortOperate = new JButton("关闭串口");
	private JButton sendData = new JButton("发送数据");
   
	byte[] data;
	private List<String> commList = null;
	private SerialPort serialport;

	public MainFrame() {
		initView();
		initComponents();
		actionListener();
		initData();
		
		//创建数据库
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:Sqlite.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      
	      String sql = "create table Tenghan (" +
	  			"electricity test, " +
				"voltage test, " +
				"sensor_Num test, " +
				"machine_id test, " +
				"welder_id test, " +
				"code test, " +
				"year test, " +
				"month test, " +
				"day test, " +
				"hour test, " +
				"minute test, " +
				"second test, " +
				"status test)"; 
	      stmt.executeUpdate(sql);
	      System.out.println("create table successfully");
	    }catch ( Exception e ) {
	    	System.out.println("The table has exist");
		    }
	}

	private void initView() {
		// 关闭程序
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		this.setLayout(null);

		setTitle("串口通讯");
	}

	private void initComponents() {
		// 数据显示
		dataView.setFocusable(false);
		scrollDataView.setBounds(10, 10, 475, 200);
		add(scrollDataView);

		// 串口设置
		serialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
		serialPortPanel.setBounds(10, 220, 170, 100);
		serialPortPanel.setLayout(null);
		add(serialPortPanel);

		serialPortLabel.setForeground(Color.gray);
		serialPortLabel.setBounds(10, 25, 40, 20);
		serialPortPanel.add(serialPortLabel);

		commChoice.setFocusable(false);
		commChoice.setBounds(60, 25, 100, 20);
		serialPortPanel.add(commChoice);

		baudrateLabel.setForeground(Color.gray);
		baudrateLabel.setBounds(10, 60, 40, 20);
		serialPortPanel.add(baudrateLabel);

		baudrateChoice.setFocusable(false);
		baudrateChoice.setBounds(60, 60, 100, 20);
		serialPortPanel.add(baudrateChoice);

		// 操作
		operatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
		operatePanel.setBounds(200, 220, 285, 100);
		operatePanel.setLayout(null);
		add(operatePanel);

		serialPortOperate.setFocusable(false);
		serialPortOperate.setBounds(155, 40, 90, 30);
		operatePanel.add(serialPortOperate);

		sendData.setFocusable(false);
		sendData.setBounds(45, 40, 90, 30);
		operatePanel.add(sendData);
	}

	@SuppressWarnings("unchecked")
	private void initData() {
		commList = SerialPortManager.findPort();
		// 检查是否有可用串口，有则加入选项中
		if (commList == null || commList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			for (String s : commList) {
				commChoice.addItem(s);
			}
		}

		baudrateChoice.addItem("9600");
		baudrateChoice.addItem("19200");
		baudrateChoice.addItem("38400");
		baudrateChoice.addItem("57600");
		baudrateChoice.addItem("115200");
	}

	private void actionListener() {
		serialPortOperate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("关闭串口".equals(serialPortOperate.getText())
						&& serialport == null) {
					//openSerialPort(e);
				} else {
					closeSerialPort(e);
				}
			}
		});

		sendData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendData(e);
			}
		});
	}


	/**
	 * 打开串口
	 * 
	 * @param evt
	 *            点击事件
	 *//*
	private void openSerialPort(java.awt.event.ActionEvent evt) {
		// 获取串口名称
		String commName = (String) commChoice.getSelectedItem();
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) baudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				serialport = SerialPortManager.openPort(commName, baudrate);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
					serialPortOperate.setText("关闭串口");
				}
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		try {
			SerialPortManager.addListener(serialport, new SerialListener());
		} catch (TooManyListeners e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * 关闭串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void closeSerialPort(java.awt.event.ActionEvent evt) {
		serialport.removeEventListener();
		SerialPortManager.closePort(serialport);
		dataView.setText("串口已关闭" + "\r\n");
		serialPortOperate.setText("关闭串口");
	}

	/**
	 * 发送数据
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void sendData(java.awt.event.ActionEvent evt) {
		
		//初始化
		OutputStream out = null;
		byte[] order = null;
		InputStream in = null;
		byte[] bytes = null;
		
		
		//打开串口
		String commName = (String) commChoice.getSelectedItem();
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) baudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				if(serialport!=null){
					serialport.removeEventListener();
					serialport.close();
				}
				serialport = SerialPortManager.openPort(commName, baudrate);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
				}
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		try {
			SerialPortManager.addListener(serialport, new SerialListener());
		} catch (TooManyListeners e) {
			e.printStackTrace();
		}
    	
		
		
		
		Timer tExit = null; 
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() { 
				Date date=new Date();
		    	SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
		    	String time=format.format(date);
		    	String time1="";
		    	String time2="";
		    	
		    	for (int i = 0; i < time.length()/2; i++)
				{
					String tstr1=time.substring(i*2, i*2+2);
					Integer k=Integer.valueOf(tstr1).intValue();
					time1=Integer.toHexString(k);
					time1=time1.toUpperCase();
					if(time1.length()==1){
		    			time1='0'+time1;
		        	}
					time2+=time1;
				}
		    	
		    	String data1 = "FAFFFF0804";
		    	String data2 = time2;
		    	String data3 = data1+data2;
		    	int data4 = 0;
		    	
			     for (int i11 = 1; i11 < data3.length()/2; i11++)
			     {
			    	String tstr1=data3.substring(i11*2, i11*2+2);
			    	data4+=Integer.valueOf(tstr1,16);
			     }
			    String data5 = ((Integer.toHexString(data4)).toUpperCase()).substring(1,3);
		    	String data  = data1+data2+data5+"F5";
		    	try {
					SerialPortManager.sendToPort(serialport,
							ByteUtils.hexStr2Byte(data));
				} catch (SendDataToSerialPortFailure e) {
					e.printStackTrace();
				} catch (SerialPortOutputStreamCloseFailure e) {
					e.printStackTrace();
				}
            }  
        }, 0,30*100); 
    	
    	
    	
    	
    	
    	
    	/*Timer tExit = null; 
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	Date date=new Date();
            	SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            	String time=format.format(date);
            	String data1 = "FEFE04";
            	String data2 = time;
            	String data3 = "010101FD";
            	String data  = data1+data2+data3;
            	try {
        			SerialPortManager.sendToPort(serialport,
        					ByteUtils.hexStr2Byte(data));
        		} catch (SendDataToSerialPortFailure e) {
        			e.printStackTrace();
        		} catch (SerialPortOutputStreamCloseFailure e) {
        			e.printStackTrace();
        		}
            }  
        }, 0,30*100); */
		
		//读取数据
		try {
			in = serialport.getInputStream();
			// 获取buffer里的数据长度
			int bufflenth = in.available();
			while (bufflenth != 0) {
				// 初始化byte数组为buffer中数据的长度
				bytes = new byte[bufflenth];
				in.read(bytes);
				bufflenth = in.available();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		/*Timer tExit = null; 
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	String data = "FE0000000000000000000000FD";
            	try {
        			SerialPortManager.sendToPort(serialport,
        					ByteUtils.hexStr2Byte(data));
        		} catch (SendDataToSerialPortFailure e) {
        			e.printStackTrace();
        		} catch (SerialPortOutputStreamCloseFailure e) {
        			e.printStackTrace();
        		}
            }  
        }, 0,30*100); */
        
		
		/*String data = dataInput.getText().toString();
		try {
			SerialPortManager.sendToPort(serialport,
					ByteUtils.hexStr2Byte(data));
		} catch (SendDataToSerialPortFailure e) {
			e.printStackTrace();
		} catch (SerialPortOutputStreamCloseFailure e) {
			e.printStackTrace();
		}*/
	}

	private class SerialListener implements SerialPortEventListener {
		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {

			case SerialPortEvent.BI: // 10 通讯中断
				ShowUtils.errorMessage("与串口设备通讯中断");
				break;

			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				
				
				try {
					Thread.sleep(20);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				 InputStream in1 = null;
				
				//读取串口数据处理
					try {
							in1 = serialport.getInputStream();
						//获取buffer里的数据长度
							int bufflenth = in1.available();
	
							while (bufflenth != 0) {                             
				                data = new byte[bufflenth];    //初始化byte数组为buffer中数据的长度
				                try {
									in1.read(data);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				                try {
									bufflenth = in1.available();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            }
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					//new Thread(sqlite).start();
					new Thread(soctran).start();
					//new Thread(websocketstart).start();
				
				
			}
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
	
	 public Runnable sqlite = new Runnable() {
			public void run() {
				String strdata = "";
				String insql;
			    // 接收服务器发送过来的消息
			    String response;
			    
			    
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					}
					
					else {
						dataView.append(ByteUtils.byteArrayToHexString(data,
								true) + "\r\n");
						
						 for(int i=0;i<data.length;i++){
                         	
                         	//判断为数字还是字母，若为字母+256取正数
                         	if(data[i]<0){
                         		String r = Integer.toHexString(data[i]+256);
                         		String rr=r.toUpperCase();
                             	System.out.print(rr);
                             	//数字补为两位数
                             	if(rr.length()==1){
                         			rr='0'+rr;
                             	}
                             	//strdata为总接收数据
                         		strdata += rr;
                         		
                         	}
                         	else{
                         		String r = Integer.toHexString(data[i]);
                             	System.out.print(r);
                             	if(r.length()==1)
                         			r='0'+r;
                         		strdata+=r;	
                         		
                         	}
                         }
                         
                         response=strdata;
                         
                         //数据写入数据库
                         String [] stringArr = strdata.split("FD");
                         for(int i =0;i < stringArr.length;i++)
         		        {
                      		String electricity = stringArr[i].subSequence(5, 9).toString();
                            String voltage = stringArr[i].subSequence(9, 13).toString();
                            String sensor_Num = stringArr[i].subSequence(13, 17).toString();
                            String machine_id = stringArr[i].subSequence(17, 21).toString();
                            String welder_id = stringArr[i].subSequence(21, 25).toString();
                            String code = stringArr[i].subSequence(25, 33).toString();
                            String year = stringArr[i].subSequence(33, 35).toString();
                            String month = stringArr[i].subSequence(35, 37).toString();
                            String day = stringArr[i].subSequence(37, 39).toString();
                            String hour = stringArr[i].subSequence(39, 41).toString();
                            String minute = stringArr[i].subSequence(41, 43).toString();
                            String second = stringArr[i].subSequence(43, 45).toString();
                            String status = stringArr[i].subSequence(45, 47).toString();
                            
                            insql = "insert into Tenghan(electricity,voltage,sensor_Num,machine_id,welder_id,code,year,month,day,hour,minute,second,status) "
                            		+ "values('"+ electricity +"','" + voltage + "','" + sensor_Num + "'"
                            				+ ","+ "'" + machine_id + "','" + welder_id + "','" + code + "'"
                            						+ ",'" + year + "','" + month + "','" + day + "','" + hour + "'"
                            								+ ",'" + minute + "','" + second + "','" + status + "')";
                            stmt.executeUpdate(insql);

         		        }
                         System.out.println("The data has writed");
                         
					}
				} catch (Exception e) {
					ShowUtils.errorMessage(e.toString());
					// 发生读取错误时显示错误信息后退出系统
					System.exit(0);
				}
			}
	 };
	 
	 public Runnable soctran = new Runnable() {
			public void run() {
				
				String strdata = "";
				String insql;
			    // 接收服务器发送过来的消息
			    String response;
				
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					}
					
					else {
						
						if(data==null){
							
						}
						else
						{			
							 for(int i=0;i<data.length;i++){
	                         	
	                         	//判断为数字还是字母，若为字母+256取正数
	                         	if(data[i]<0){
	                         		String r = Integer.toHexString(data[i]+256);
	                         		String rr=r.toUpperCase();
	                             	//数字补为两位数
	                             	if(rr.length()==1){
	                         			rr='0'+rr;
	                             	}
	                             	//strdata为总接收数据
	                         		strdata += rr;
	                         		
	                         	}
	                         	else{
	                         		String r = Integer.toHexString(data[i]);
	                             	if(r.length()==1)
	                         			r='0'+r;
	                             	r=r.toUpperCase();
	                         		strdata+=r;	
	                         		
	                         	}
	                         }
	                         response=strdata;
	                         
	                         byte[] bb3=new byte[strdata.length()/2];
	     					for (int i1 = 0; i1 < bb3.length; i1++)
	     					{
	     						String tstr1=strdata.substring(i1*2, i1*2+2);
	     						Integer k=Integer.valueOf(tstr1, 16);
	     						bb3[i1]=(byte)k.byteValue();
	     					}
	                         
	     					try {
								socket = new Socket("192.168.23.4", 5555);
							} catch (IOException e1) {
								dataView.setText("服务器连接失败" + "\r\n");
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
	                        
	                        if(socket!=null){
	                        	System.out.print("服务器连接成功"+ "\r\n");
	                        }
	                        else{
	                        	dataView.setText("服务器连接失败" + "\r\n");
	                        	System.out.print("服务器连接失败"+ "\r\n");
	                        }
						
	                    try {
	                    	//发送消息
	                        // 步骤1：从Socket 获得输出流对象OutputStream
	                        // 该对象作用：发送数据
	                        outputStream = socket.getOutputStream();
	
	                        // 步骤2：写入需要发送的数据到输出流对象中
							dataView.append(ByteUtils.byteArrayToHexString(data,
									true) + "\r\n");
	                        outputStream.write(bb3);
	                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
	
	                        // 步骤3：发送数据到服务端
	                        outputStream.flush();
	
	                        socket.close();
	     		           
	                    } catch (IOException e1) {
	                        e1.printStackTrace();
	                    }
						}
					} 
				}catch (Exception e) {

					}
				
				/*String o="FE24";
                String p="00000000FD";
                String l="";
				try {
					ResultSet rs = stmt.executeQuery( "select * from Tenghan;" );
					while ( rs.next() ) {
				         String electricity = rs.getString("electricity");
				         String voltage = rs.getString("voltage");
				         String sensor_Num  = rs.getString("sensor_Num");
				         String machine_id = rs.getString("machine_id");
				         String welder_id = rs.getString("welder_id");
				         String code = rs.getString("code");
				         String year = rs.getString("year");
				         String month = rs.getString("month");
				         String day = rs.getString("day");
				         String hour = rs.getString("hour");
				         String minute = rs.getString("minute");
				         String second = rs.getString("second");
				         String status = rs.getString("status");
				         
				         l = l + o + electricity + voltage + sensor_Num 
	                        		+ machine_id + welder_id + code + year 
	                        		+ month + day + hour + minute + second + status + p;
				         
				         String sql = "update Tenghan set status = 01";   
	                        //执行SQL   
				         stmt.executeUpdate(sql);
				         
					}
					
					byte[] bb3=new byte[l.length()/2];
					for (int i1 = 0; i1 < bb3.length; i1++)
					{
						String tstr1=l.substring(i1*2, i1*2+2);
						Integer k=Integer.valueOf(tstr1, 16);
						String s = String.valueOf(k);
						if(s.length()==1){
							s="0"+s;
						}
						bb3[i1]=(byte)k.byteValue();
					}
                    
					 try {
							socket = new Socket("192.168.8.109", 5555);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							System.out.print("socket didn't connect");
						}
                        
                        boolean j=socket.isConnected();
                        if(j){
                        	dataView.setText("socket已打开" + "\r\n");
                        	System.out.print("服务器连接成功");
                        }
                        else{
                        	System.out.print("服务器连接失败");
                        }
					
                    try {
                    	//发送消息
                        // 步骤1：从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        outputStream = socket.getOutputStream();

                        // 步骤2：写入需要发送的数据到输出流对象中
                        dataView.setText(l + "\r\n");
                        outputStream.write(bb3);
                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                        // 步骤3：发送数据到服务端
                        outputStream.flush();

     		           
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
			}
	 };
	 
	 
	 /*public Runnable websocketstart = new Runnable() {
		 private boolean hasHandshake = false;  
	        private PrintWriter getWriter(Socket socket) throws IOException {  
	            OutputStream socketOut = socket.getOutputStream();  
	            return new PrintWriter(socketOut, true);  
	        }  
			public void run() {
			
				//建立websocket连接
				try {
					
					System.out.println("S: Connecting...");  
					
					if(serverSocket==null){
						
						serverSocket = new ServerSocket(SERVERPORT);
						
						websocketlink = serverSocket.accept();  
						  
						System.out.println("S: Receiving...");  
		
						
						//获取socket输入流信息  
		                InputStream in = websocketlink.getInputStream(); 
		                
		                PrintWriter pw = getWriter(websocketlink);
		                
		                //读入缓存(定义一个1M的缓存区)  
		                byte[] buf = new byte[1024]; 
		                
		                //读到字节（读取输入流数据到缓存）  
		                int len = in.read(buf, 0, 1024);
		                
		                //读到字节数组（定义一个容纳数据大小合适缓存区）  
		                byte[] res = new byte[len];  
		                
		                //将buf内中数据拷贝到res中  
		                System.arraycopy(buf, 0, res, 0, len); 
		                
		                //打印res缓存内容  
		                String key = new String(res);  
		                if(!hasHandshake && key.indexOf("Key") > 0){  
		                    //握手  
		                    //通过字符串截取获取key值  
		                    key = key.substring(0, key.indexOf("==") + 2);  
		                    key = key.substring(key.indexOf("Key") + 4, key.length()).trim();  
		                    //拼接WEBSOCKET传输协议的安全校验字符串  
		                    key+= "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";  
		                    //通过SHA-1算法进行更新  
		                    MessageDigest md = null;
							try {
								md = MessageDigest.getInstance("SHA-1");
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}    
		                    md.update(key.getBytes("utf-8"), 0, key.length());  
		                    byte[] sha1Hash = md.digest();    
		                    //进行Base64加密  
		                    sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();    
		                    key = encoder.encode(sha1Hash); 
		                    pw.println("HTTP/1.1 101 Switching Protocols");  
		                    pw.println("Upgrade: websocket");  
		                    pw.println("Connection: Upgrade");  
		                    pw.println("Sec-WebSocket-Accept: " + key);  
		                    pw.println();  
		                    pw.flush();  
		                    //将握手标志更新，只握一次  
		                    hasHandshake = true;  
		    				
		                }

	                }
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally{
					
    				if(websocketlink!=null){

    					new Thread(websocket).start();
    					
    				}
    				
				}
				
			}
	 };
	 
	 
	 
	 public Runnable websocket = new Runnable() {
			public void run() {
				Session session;
				String strdata = "";
				String strsend = "";
			    String response;
				try {
					
    					if (serialport == null) {
    						ShowUtils.errorMessage("串口对象为空！监听失败！");
    					}
    					else{
    						
    						if(data==null){
    							
    						}
    						else
    						{	
    							
    							//串口数据处理
    							for(int i=0;i<data.length;i++){
    	                         	
    	                         	//判断为数字还是字母，若为字母+256取正数
    	                         	if(data[i]<0){
    	                         		String r = Integer.toHexString(data[i]+256);
    	                         		String rr=r.toUpperCase();
    	                             	//数字补为两位数
    	                             	if(rr.length()==1){
    	                         			rr='0'+rr;
    	                             	}
    	                             	//strdata为总接收数据
    	                         		strdata += rr;
    	                         		
    	                         	}
    	                         	else{
    	                         		String r = Integer.toHexString(data[i]);
    	                             	if(r.length()==1)
    	                         			r='0'+r;
    	                             	r=r.toUpperCase();
    	                         		strdata+=r;	
    	                         		
    	                         	}
    	                         }
    	                             
    							 String weldname = strdata.substring(10,14);
    							 String welder=strdata.substring(14,18);
    	    					 String electricity1=strdata.substring(26,30);
    	    					 String voltage1=strdata.substring(30,34);
    	    					 String status1=strdata.substring(38,40);
    	                         
    	    					 String electricity2=strdata.substring(52,56);
    	    					 String voltage2=strdata.substring(56,60);
    	    					 String status2=strdata.substring(64,66);
    	    					 
    	    					 String electricity3=strdata.substring(78,82);
    	    					 String voltage3=strdata.substring(82,86);
    	    					 String status3=strdata.substring(90,92);
    	    					 
    	                         DB_Connection a =new DB_Connection();
    	                         
    	                         String dbdata = a.getId();
    	                         
    	                         for(int i=0;i<dbdata.length();i+=12){
		                        	 String status=dbdata.substring(0+i,2+i);
		                        	 String framework=dbdata.substring(2+i,4+i);
	    	                         String weld=dbdata.substring(4+i,8+i); 
	    	                         String position=dbdata.substring(8+i,12+i);
	    	                         if(weldname.equals(weld)){
	    	                        	 strsend+=status1+framework+weld+position+welder+electricity1+voltage1
	    	                        			 +status2+framework+weld+position+welder+electricity2+voltage2
	    	                        			 +status3+framework+weld+position+welder+electricity3+voltage3;
	    	                         }
	    	                         else{
	    	                        	 strsend+=status+framework+weld+position+"0000"+"0000"+"0000"
	    	                        			 +status+framework+weld+position+"0000"+"0000"+"0000"
	    	                        			 +status+framework+weld+position+"0000"+"0000"+"0000";
	    	                         }
    	                         }
                 
    	                        //数据发送
    	                        byte[] bb3=strsend.getBytes();
    		                      
    							ByteBuffer byteBuf = ByteBuffer.allocate(bb3.length);
    							
    							for(int j=0;j<bb3.length;j++){
    								byteBuf.put(bb3[j]);
    							}
    							byteBuf.flip();
    	                        
    	                        //将内容返回给客户端  
    	                        responseClient(byteBuf, true, websocketlink); 
    	                        
    	                        dataView.setText("实时数据发送" + "\r\n");
    	                        System.out.println("实时数据已发送");
		    					
    						}
    					}
  
	                
					
	                
	                    
	                byte[] first = new byte[1];  
                    //这里会阻塞  
                    int read = in.read(first, 0, 1);  
                    //读取第一个字节是否有值,开始接收数据  
                    while(read > 0){  
                        //让byte和十六进制做与运算（二进制也就是11111111）  
                        //获取到第一个字节的数值  
                        int b = first[0] & 0xFF;  
                        //1为字符数据，8为关闭socket（只要低四位的值判断）  
                        byte opCode = (byte) (b & 0x0F);  
                        if(opCode == 8){  
                            socket.getOutputStream().close();  
                            break;  
                        }  
                        b = in.read();  
                        //只能描述127  
                        int payloadLength = b & 0x7F;  
                        if (payloadLength == 126) {  
                            byte[] extended = new byte[2];  
                            in.read(extended, 0, 2);  
                            int shift = 0;  
                            payloadLength = 0;  
                            for (int i = extended.length - 1; i >= 0; i--) {  
                                payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);  
                                shift += 2;  
                            }  
                        } else if (payloadLength == 127) {  
                            byte[] extended = new byte[8];  
                            in.read(extended, 0, 8);  
                            int shift = 0;  
                            payloadLength = 0;  
                            for (int i = extended.length - 1; i >= 0; i--) {  
                                payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);  
                                shift += 8;  
                            }  
                        }  
                        //掩码  
                        byte[] mask = new byte[4];  
                        in.read(mask, 0, 4);  
                        int readThisFragment = 1;  
                        ByteBuffer byteBuf = ByteBuffer.allocate(payloadLength + 30);  
                        byteBuf.put("浏览器: ".getBytes("UTF-8"));  
                        while(payloadLength > 0){  
                             int masked = in.read();  
                             masked = masked ^ (mask[(int) ((readThisFragment - 1) % 4)] & 0xFF);  
                             byteBuf.put((byte) masked);  
                             payloadLength--;  
                             readThisFragment++;  
                        }  
                        byteBuf.flip();  
                        //将内容返回给客户端  
                        responseClient(byteBuf, true, socket);  
                        //打印内容    
                        in.read(first, 0, 1);  
                    }  
                    
                    
                }
					
					String b="FE115555555555555555550EFD";
				    byte[] bb=new byte[b.length()/2];

					for (int i = 0; i < bb.length; i++)
					{
						String tstr1=b.substring(i*2, i*2+2);
						Integer k=Integer.valueOf(tstr1, 16);
						bb[i]=(byte)k.byteValue();
					}
					responseClient(bb,true,socket);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						websocketlink.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					dataView.setText("实时数据发送失败" + "\r\n");
					System.out.println("实时数据发送失败");
					e.printStackTrace();
				}
			}
	 };
	 
	 public void responseClient(ByteBuffer byteBuf, boolean finalFragment,Socket socket) throws IOException {  
         OutputStream out = websocketlink.getOutputStream();  
         int first = 0x00;  
         //是否是输出最后的WebSocket响应片段  
             if (finalFragment) {  
                 first = first + 0x80;  
                 first = first + 0x1;  
             }  
             out.write(first);  
             if (byteBuf.limit() < 126) {  
                 out.write(byteBuf.limit());  
             } else if (byteBuf.limit() < 65536) {  
             out.write(126);  
             out.write(byteBuf.limit() >>> 8);  
             out.write(byteBuf.limit() & 0xFF);  
             } else {  
             // Will never be more than 2^31-1  
             out.write(127);  
             out.write(0);  
             out.write(0);  
             out.write(0);  
             out.write(0);  
             out.write(byteBuf.limit() >>> 24);  
             out.write(byteBuf.limit() >>> 16);  
             out.write(byteBuf.limit() >>> 8);  
             out.write(byteBuf.limit() & 0xFF);  
             }  
             // Write the content  
             out.write(byteBuf.array(), 0, byteBuf.limit());  
             out.flush();  
     }  */
 
 }  
	 
