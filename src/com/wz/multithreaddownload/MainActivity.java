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
	// �����߳������ܽ���
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
				// ����http�����õ�Ŀ���ļ�����

				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);

					if (conn.getResponseCode() == 200) {
						// ��ȡ����
						int length = conn.getContentLength();

						// ������ʱ�ļ�
						File file = new File(
								Environment.getExternalStorageDirectory(),
								getNameFromPath(path));
						/**
						 * RandomAccessFile�� RandomAccessFile���ṩ���ڶ���ļ����ʷ�����
						 * RandomAccessFile��֧�֡��������"��ʽ����������ʣ��ǿ��Դ��ļ�������һ�㿪ʼ��ȡ��
						 * RandomAccessFile�������
						 * �����˳����ԣ���д�ȳ���¼��ʽ���ļ�ʱ�кܴ�����ơ������磺��ȡ���ݿ��е�ĳһ����¼ʱ��
						 * RandomAccessFile������ڲ����ļ������ܷ���������IO�豸�������磬�ڴ�ӳ��ȡ�
						 * ���ֹ��췽���� new RandomAccessFile(f,"rw"); //��д��ʽ new
						 * RandomAccessFile(f,"r"); //ֻ����ʽ "r" ��ֻ����ʽ�򿪡����ý��������κ�
						 * write �������������׳� IOException�� "rw"
						 * ���Ա��ȡ��д�롣������ļ��в����ڣ����Դ������ļ��� "rws" ���Ա��ȡ��д�룬����
						 * "rw"����Ҫ����ļ������ݻ�Ԫ���ݵ�ÿ�����¶�ͬ��д�뵽�ײ�洢�豸�� "rwd"
						 * ���Ա��ȡ��д�룬���� "rw"����Ҫ����ļ����ݵ�ÿ�����¶�ͬ��д�뵽�ײ�洢�豸�� "rws" ��
						 * "rwd" ģʽ�Ĺ�����ʽ�������� FileChannel ��� force(boolean)
						 * �������ֱ𴫵� true �� false ��������������ʼ��Ӧ����ÿ�� I/O
						 * �����������ͨ����Ϊ��Ч��������ļ�λ�ڱ��ش洢�豸��
						 * ����ô�����ش����һ�������ĵ���ʱ�����Ա�֤�ɸõ��öԴ��ļ����������и��ľ���д����豸��
						 * ���ȷ����ϵͳ����ʱ���ᶪʧ��Ҫ��Ϣ�ر����á�������ļ����ڱ����豸�ϣ����޷��ṩ�����ı�֤��
						 * 
						 * "rwd" ģʽ�����ڼ���ִ�е� I/O ����������ʹ�� "rwd"
						 * ��Ҫ�����Ҫд��洢���ļ������ݣ�ʹ�� "rws"
						 * Ҫ�����Ҫд����ļ����ݼ���Ԫ���ݣ���ͨ��Ҫ������һ�����ϵĵͼ��� I/O ������
						 * 
						 * ������ڰ�ȫ����������ʹ�� file ������·������Ϊ������������� checkRead
						 * �������Բ鿴�Ƿ�����Ը��ļ����ж�ȡ���ʡ������ģʽ����д�룬��ô��ʹ�ø�·���������øð�ȫ��������
						 * checkWrite �������Բ鿴�Ƿ�����Ը��ļ�����д����ʡ�
						 */
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");
						//������ʱ�ļ���С��Ŀ���ļ�һ��
						raf.setLength(length);
						raf.close();
						
						//���ý����������ֵ
						pb.setMax(length);
						
						//����ÿ���߳���������
						int size = length / threadCount;
						for(int id = 0; id < threadCount;id++) {
							//����ÿ���߳����صĿ�ʼλ�úͽ���λ��
							int startIndex = id * size;
							int endIndex = (id + 1) * size - 1;
							if(id == threadCount - 1) { //�����޷�ƽ��ʱ�����һ�ζ�Ҫ�������һ���߳�
								endIndex = length -1;
							}
							Log.i("TAG", "�߳�" + id + "���ص����䣺" + startIndex + " ~ " + endIndex);
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
				int lastProgress = 0; //��¼��һ�����صĽ���
				if(fileProgress.exists()) {
					//��ȡ������ʱ�ļ��е�����
					FileInputStream fis = new FileInputStream(fileProgress);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					//�õ���һ�����ؽ���
					lastProgress = Integer.parseInt(br.readLine());
					//�ı����صĿ�ʼλ�ã���һ�����ع��ģ���ξͲ�������
					startIndex += lastProgress;
					fis.close();
					
					//����һ�����ؽ��ȼ��ص�������������
					downloadProgress += lastProgress;
					pb.setProgress(downloadProgress);
					
					//������Ϣ�����ı��������ı� 
					handler.sendEmptyMessage(1);
				}
				
				//����http��������Ҫ���ص�����
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setReadTimeout(8000);
				conn.setConnectTimeout(8000);
				//�����������ݵ�����
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				
				//���󲿷����ݣ��ɹ�����Ӧ����206
				if(conn.getResponseCode() == 206) {
					InputStream is = conn.getInputStream();
					byte[] b = new byte[1024];
					int len = 0;
					//��ǰ�߳����ص��ܽ���
					int total = lastProgress;
					File file = new File(Environment.getExternalStorageDirectory(),getNameFromPath(path));
					RandomAccessFile raf = new RandomAccessFile(file, "rwd");
					//����д��Ŀ�ʼλ��
					raf.seek(startIndex);
					while((len = is.read(b)) != -1) {
						raf.write(b, 0, len);
						total += len;
						Log.i("TAG","�߳�" + threadId + "�����ˣ�" + total);
						
						//����һ��������ʱ�ļ����������ؽ���
						RandomAccessFile rafProgress = new RandomAccessFile(fileProgress, "rwd");
						//ÿ������1024���ֽ�,�����ϰ�1024д�����ʱ�ļ�
						rafProgress.write((total + "").getBytes());
						rafProgress.close();
						
						//ÿ������len�����ȵ��ֽڣ����ϰ�len�ӵ����ؽ����У��ý������ܷ�Ӧ��len�����ȵ����ؽ���
						downloadProgress += len;
						pb.setProgress(downloadProgress);
						
						//������Ϣ�����ı��������ı�
						handler.sendEmptyMessage(1);
					}
					raf.close();
					System.out.println("�߳�" + threadId + "�������------------------");
					
					//3���߳�ȫ��������ϣ���ȥɾ��������ʱ�ļ�
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
