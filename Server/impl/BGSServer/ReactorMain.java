package bgu.spl.net.impl.BGSServer;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Database;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args){
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        Server server = Server.reactor(
                threads,
                port, //port
                ()->new BidiMessagingProtocolImpl<>(), //protocol factory
                ()->new MessageEncoderDecoderImpl()); //message encoder decoder factory

        server.serve();
    }


}