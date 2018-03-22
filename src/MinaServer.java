import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MinaServer {

    /**
     * 所有Session
     * */
    public List<IoSession> sessions = new ArrayList<>();
    /**
     * 房间列表
     * containkey
     * */
    public HashMap<String,GameRoom> rooms = new HashMap<>();
    /**
     * 线程列表
     * */
    public HashMap<String,Thread> threads = new HashMap<>();

    private static final int PORT = 9123;
    NioSocketAcceptor acceptor = new NioSocketAcceptor();

    public void init(){
        if (acceptor == null){
            acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
        }

        acceptor.setReuseAddress(true);
        acceptor.getSessionConfig().setReadBufferSize(8192);

        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new Factory()));

        acceptor.setHandler(new MinaServerHandler());

        try {
            acceptor.bind( new InetSocketAddress(PORT) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class MinaServerHandler extends IoHandlerAdapter {

        public MinaServerHandler() {
            super();
        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            System.out.println("SessionCreated");
            System.out.println(session.getId());
            sessions.add(session);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            System.out.println("SessionOpened");
            System.out.println(session.getId());
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            System.out.println("SessionClosed");
            System.out.println(session.getId());
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            System.out.println("SessionIdle");
            System.out.println(session.getId());
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            System.out.println("ExceptionCaught");
            System.out.println(session.getId() + cause.getMessage());
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            MyData myData  = (MyData)message;

            switch (myData.getType()){
                case 0:
                    //下棋
                    break;
                case 1:
                    GameRoom gameRoom = new GameRoom(myData.getRoomName());
                    gameRoom.setSession1(session);
                    rooms.put(gameRoom.getName(),gameRoom);
                    //建立房间
                    break;
                case 2:
                    //加入房间
                    if(rooms.containsKey(myData.getRoomName()) && rooms.get(myData.getRoomName()).getSession2() == null){
                        //成功加入房间
                        GameRoom temp = rooms.get(myData.getRoomName());
                        temp.setSession2(session);
                        rooms.put(myData.getRoomName(),temp);
                    } else {
                        //返回错误的数据，房间已满或者不存在房间
                    }
                    break;
                default:
                        break;
            }
            System.out.println("MessageReceived");

//
//            if(!msg.isData()){
//                System.out.println(msg.getMessage());
//            } else {
//                System.out.println("这是一个数据文件");
//            }
//            System.out.println(session.getId());
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println("MessageSent");
            System.out.println(session.getId());
        }
    }
}
