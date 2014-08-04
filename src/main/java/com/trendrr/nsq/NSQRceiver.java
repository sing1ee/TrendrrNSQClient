/**
 * 
 */
package com.trendrr.nsq;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.trendrr.nsq.lookup.NSQLookupDynMapImpl;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;


/**
 * @author zhangcheng
 *
 */
public class NSQRceiver extends Receiver<String> {
	
	
	static class LookupAddr {
		public String host;
		public int port;
		
		public String toString() {
			return host + ":" + port;
		}
	}
	
	private NSQConsumer consumer;
	private static AtomicInteger processed = new AtomicInteger(0);

	public NSQRceiver(List<LookupAddr> lookupServs, String topic) {
		super(StorageLevel.MEMORY_AND_DISK_2());
		
		NSQLookup lookup = new NSQLookupDynMapImpl();
		for (LookupAddr addr : lookupServs) {
			System.out.println("add lookup serv:" + addr.toString());
			lookup.addAddr(addr.host, addr.port);
		}
		consumer = new NSQConsumer(lookup, topic, "java_ss",
				new NSQMessageCallback() {

					/**
					 * 
					 */
					private static final long serialVersionUID = -5309194588376755411L;

					@Override
					public void message(NSQMessage message) {
						try {

							// now mark the message as finished.
							message.finished();

							// or you could requeue it, which indicates a
							// failure and puts it back on the queue.
							// message.requeue();

							int p = processed.incrementAndGet();
							System.out.println("consumer: " + p);
							
							try {
								byte[] body = message.getMessage();
//								store(ByteBuffer.wrap(body));
								store(new String(body, "utf8"));
							} catch (Exception e) {
								message.requeue();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void error(Exception x) {
						x.printStackTrace();
					}
				});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8371889844783884397L;
	
	@Override
	public void onStart() {
		System.out.println("start receiver");
		new Thread(new Runnable(){

			@Override
			public void run() {
				consumer.start();
			}}).start();
//		exec.submit(new Runnable(){
//
//			@Override
//			public void run() {
//				consumer.start();
//			}});
	}

	@Override
	public void onStop() {
		if (null != consumer) {
			consumer.close();
		}
	}

}
