import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * @program: MinaClient
 * @description:
 * @author: Yang Yang
 * @create: 2018-03-21 16:49
 **/
public class Factory implements ProtocolCodecFactory {
    private Encoder encoder;
    private Decoder decoder;

    public Factory() {
        encoder = new Encoder();
        decoder = new Decoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
