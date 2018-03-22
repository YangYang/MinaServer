import org.apache.mina.core.session.IoSession;

public class GameRoom {

    private String name;
    private IoSession session1 = null;
    private IoSession session2 = null;

    public GameRoom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IoSession getSession1() {
        return session1;
    }

    public void setSession1(IoSession session1) {
        this.session1 = session1;
    }

    public IoSession getSession2() {
        return session2;
    }

    public void setSession2(IoSession session2) {
        this.session2 = session2;
    }
}
