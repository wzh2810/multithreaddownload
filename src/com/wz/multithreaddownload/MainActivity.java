package com.wz.multithreaddownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	String path = "http://192.168.3.17:8080/Git-2.7.2-64-bit.exe";
	int threadCount = 3;
	int finidshedThread = 0;
	// 所有线程下载总进度
	int downloadProgress = 0;
	private ProgressBar pb;
	private TextView tv;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			tv.setText((long)pb.getProgress() * 100 / pb.getMax() + "%");
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pb = (ProgressBar) findViewById(R.id.pb);
		tv = (TextView) findViewById(R.id.tv);
	}

	public void click(View v) {
		Thread t = new Thread() {
			@Override
			public void run() {
				// 发送http请求，拿到目标文件长度

				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);

					if (conn.getResponseCode() == 200) {
						// 获取长度
						int length = conn.getContentLength();

						// 创建临时文件
						File file = new File(
								Environment.getExternalStorageDirectory(),
								getNameFromPath(path));
						/**
						 * RandomAccessFile类 RandomAccessFile类提供了众多的文件访问方法。
						 * RandomAccessFile类支持“随机访问"方式。（随机访问：是可以从文件中任意一点开始读取）
						 * RandomAccessFile类在随机
						 * （相对顺序而言）读写等长记录格式的文件时有很大的优势。（比如：读取数据库中的某一条记录时）
						 * RandomAccessFile类仅限于操作文件，不能访问其他的IO设备，如网络，内存映象等。
						 * 两种构造方法： new RandomAccessFile(f,"rw"); //读写方式 new
						 * RandomAccessFile(f,"r"); //只读方式 "r" 以只读方式打开。调用结果对象的任何
						 * write 方法都将导致抛出 IOException。 "rw"
						 * 打开以便读取和写入。如果该文件尚不存在，则尝试创建该文件。 "rws" 打开以便读取和写入，对于
						 * "rw"，还要求对文件的内容或元数据的每个更新都同步写入到底层存储设备。 "rwd"
						 * 打开以便读取和写入，对于 "rw"，还要求对文件内容的每个更新都同步写入到底层存储设备。 "rws" 和
						 * "rwd" 模式的工作方式极其类似 FileChannel 类的 force(boolean)
						 * 方法，分别传递 true 和 false 参数，除非它们始终应用于每个 I/O
						 * 操作，并因此通常更为高效。如果该文件位于本地存储设备上
						 * ，那么当返回此类的一个方法的调用时，可以保证由该调用对此文件所做的所有更改均被写入该设备。
						 * 这对确保在系统崩溃时不会丢失重要信息特别有用。如果该文件不在本地设备上，则无法提供这样的保证。
						 * 
						 * "rwd" 模式可用于减少执行的 I/O 操作数量。使用 "rwd"
						 * 仅要求更新要写入存储的文件的内容；使用 "rws"
						 * 要求更新要写入的文件内容及其元数据，这通常要求至少一个以上的低级别 I/O 操作。
						 * 
						 * 如果存在安全管理器，则使用 file 参数的路径名作为其参数调用它的 checkRead
						 * 方法，以查看是否允许对该文件进行读取访问。如果该模式允许写入，那么还使用该路径参数调用该安全管理器的
						 * checkWrite 方法，以查看是否允许对该文件进行写入访问。
						 */
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");
						//设置临时文件大小与目标文件一致
						raf.setLength(length);
						raf.close();
						
						//设置进度条的最大值
						pb.setMax(length);
						
						//计算每个线程下载区间
						int size = length / threadCount;
						for(int id = 0; id < threadCount;id++) {
							//计算每个线程下载的开始位置和结束位置
							int startIndex = id * size;
							int endIndex = (id + 1) * size - 1;
							if(id == threadCount - 1) { //长度无法平分时，最后一段都要交给最后一个线程
								endIndex = length -1;
							}
							Log.i("TAG", "线程" + id + "下载的区间：" + startIndex + " ~ " + endIndex);
							new DownLoadThread(id,startIndex,endIndex).start();
						}
					
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	public String getNameFromPath(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index + 1);
	}
	
	class DownLoadThread extends Thread{
		int threadId;
		int startIndex;
		int endIndex;
		public DownLoadThread(int threadId, int startIndex, int endIndex) {
			super();
			this.threadId = threadId;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		
		@Override
		public void run() {
			try {
				File fileProgress = new File(Environment.getExternalStorageDirectory(),threadId + ".txt");
				int lastProgress = 0; //记录上一次下载的进度
				if(fileProgress.exists()) {
					//读取进度临时文件中的内容
					FileInputStream fis = new FileInputStream(fileProgress);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					//得到上一次下载进度
					lastProgress = Integer.parseInt(br.readLine());
					//改变下载的开始位置，上一次下载过的，这次就不请求了
					startIndex += lastProgress;
					fis.close();
					
					//把上一次下载进度加载到进度条进度中
					downloadProgress += lastProgress;
					pb.setProgress(downloadProgress);
					
					//发送消息，让文本进度条改变 
					handler.sendEmptyMessage(1);
				}
				
				//发送http请求，请求要下载的数据
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setReadTimeout(8000);
				conn.setConnectTimeout(8000);
				//设置请求数据的区间
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				
				//请求部分数据，成功的相应码是206
				if(conn.getResponseCode() == 206) {
					InputStream is = conn.getInputStream();
					byte[] b = new byte[1024];
					int len = 0;
					//当前线程下载的总进度
					int total = lastProgress;
					File file = new File(Environment.getExternalStorageDirectory(),getNameFromPath(path));
					RandomAccessFile raf = new RandomAccessFile(file, "rwd");
					//设置写入的开始位置
					raf.seek(startIndex);
					while((len = is.read(b)) != -1) {
						raf.write(b, 0, len);
						total += len;
						Log.i("TAG","线程" + threadId + "下载了：" + total);
						
						//创建一个进度临时文件，保存下载进度
						RandomAccessFile rafProgress = new RandomAccessFile(fileProgress, "rwd");
						//每次下载1024个字节,就马上把1024写入进临时文件
						rafProgress.write((total + "").getBytes());
						rafProgress.close();
						
						//每次下载len个长度的字节，马上把len加到下载进度中，让进度条能反应这len个长度的下载进度
						downloadProgress += len;
						pb.setProgress(downloadProgress);
						
						//发送消息，让文本进度条改变
						handler.sendEmptyMessage(1);
					}
					raf.close();
					System.out.println("线程" + threadId + "下载完毕------------------");
					
					//3条线程全部下载完毕，才去删除进度临时文件
					finidshedThread++;
					synchronized (path) {
						if(finidshedThread == threadCount) {
							for(int i = 0; i < threadCount; i++) {
								File f = new File(Environment.getExternalStorageDirectory(),i + ".txt");
								f.delete();
							}
							finidshedThread = 0;
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};

}
