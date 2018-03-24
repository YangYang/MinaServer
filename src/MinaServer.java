import MyData.MyData;
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
            //在这里告诉已经连线的session对方下线
            for(String key : rooms.keySet()){
                GameRoom gameRoom = rooms.get(key);
                if(gameRoom.getSession1() == session && gameRoom.getSession2()!=null){
                    //通知session2
                    MyData myData = new MyData();
                    myData.setType(4);
                    gameRoom.getSession2().write(myData);
                } else if(gameRoom.getSession2() == session && gameRoom.getSession1()!=null){
                    //通知session1
                    MyData myData = new MyData();
                    myData.setType(4);
                    gameRoom.getSession1().write(myData);
                }
            }

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
            GameRoom gameRoom = new GameRoom(myData.getRoomName());
            switch (myData.getType()){
                case 0:
                    //下棋
                    IoSession session1 = rooms.get(gameRoom.getName()).getSession1();
                    IoSession session2 = rooms.get(gameRoom.getName()).getSession2();
                    MyData tempM = (MyData) message;
                    System.out.println(tempM);
                    if(session == session1){
                        //1 to 2
                        session2.write(message);
                    } else if(session == session2) {
                        //2 to 1
                        session1.write(message);
                    } else {
                        System.out.println("不知道什么错误");
                    }
                    break;
                case 1:
                    boolean haveRoom = false;
                    //如果该session之前申请过房间，抹除之前的记录
                    for(String key : rooms.keySet()){
                        GameRoom temp = rooms.get(key);
                        if(temp.getSession1().getId() == session.getId()){
                            rooms.remove(temp.getName());
                            temp.setName(gameRoom.getName());
                            rooms.put(temp.getName(),temp);
                            haveRoom = true;
                            break;
                        }
                    }
                    //没有的话就加入room
                    if(!haveRoom){
                        gameRoom.setSession1(session);
                        rooms.put(gameRoom.getName(),gameRoom);
                    }
                    //建立房间
                    break;
                case 2:
                    //加入房间
                    if(rooms.containsKey(myData.getRoomName()) && rooms.get(myData.getRoomName()).getSession2() == null){
                        if(rooms.get(myData.getRoomName()).getSession1() == session){
                            //不能加入自己的房间
                            MyData temp = new MyData();
                            temp.setType(-1);
                            session.write(temp);
                            break;
                        }
                        //成功加入房间
                        GameRoom temp = rooms.get(myData.getRoomName());
                        temp.setSession2(session);
                        rooms.put(myData.getRoomName(),temp);
                        MyData tempMyData = new MyData();
                        //上线通知
                        tempMyData.setType(3);
                        tempMyData.setRoomName(temp.getName());
                        temp.getSession1().write(tempMyData);
                        temp.getSession2().write(tempMyData);

                    } else {
                        //返回错误的数据，房间已满或者不存在房间
                        MyData temp = new MyData();
                        temp.setType(-2);
                        session.write(temp);
                    }
                    break;

                case 5:
                    //留下
                    MyData temp = (MyData) message;
                    GameRoom tempRoom = rooms.get(temp.getRoomName());
                    tempRoom.setSession1(session);
                    tempRoom.setSession2(null);
                    rooms.put(tempRoom.getName(),tempRoom);
                    break;
                case 6:
                    //离开
                    MyData tempMyData = (MyData) message;
                    rooms.remove(tempMyData.getRoomName());
                    break;
                default:
                    break;
            }
            System.out.println("MessageReceived");
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println("MessageSent");
            System.out.println(session.getId());
        }
    }
}
