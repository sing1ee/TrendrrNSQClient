## TrendrrNSQCLient
=============

- Please Note, I am not actively maintaining this codebase.  If anyone wants to maintain a fork I can link to it.

A fast netty based java client for [nsq][nsq].  We developed this client to use in various places in the trendrr.tv and curatorr.com stacks.
It is currently deployed in production.  It produces and consumes billions of messages per day. 


## Consumer

Example usage:

```
NSQLookup lookup = new NSQLookupDynMapImpl();
lookup.addAddr("localhost", 4161);
NSQConsumer consumer = new NSQConsumer(lookup, "speedtest", "dustin", new NSQMessageCallback() {
            
    @Override
    public void message(NSQMessage message) {
        System.out.println("received: " + message);            
        //now mark the message as finished.
        message.finished();
        
        //or you could requeue it, which indicates a failure and puts it back on the queue.
        //message.requeue();
    }           
    @Override
    public void error(Exception x) {
        //handle errors
        log.warn("Caught", x);
    }
});
        
consumer.start();
```


## Producer

Example usage: 

```
NSQProducer producer = new NSQProducer().addAddress("localhost", 4150, 1);            
producer.start();
for (int i=0; i < 50000; i++) {
    producer.produce("speedtest", ("this is a message" + i).getBytes());
}
```

The producer also has a Batch collector that will collect messages until some threshold is reached (currently maxbytes or maxmessages) then send as a MPUB request.  This gives much greater throughput then producing messages one at a time.

```
producer.configureBatch("speedtest", 
                new BatchCallback() {
                    @Override
                    public void batchSuccess(String topic, int num) {
                    }
                    @Override
                    public void batchError(Exception ex, String topic, List<byte[]> messages) {
                        ex.printStackTrace();   
                    }
                }, 
            batchsize, 
            null, //use default maxbytes 
            null //use default max seconds
        );

producer.start();
for (int i=0; i < iterations; i++) {
    producer.produceBatch("speedtest", ("this is a message" + i).getBytes());
}
```

## NOTE

change from oss to org.codehaus.jackson

## build

`mvn package`

