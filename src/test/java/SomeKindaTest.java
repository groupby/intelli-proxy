import com.github.terma.javaniotcpproxy.TcpProxy;
import com.github.terma.javaniotcpproxy.TcpProxyConfig;
import org.junit.Test;

/**
 * Created by will on 17/05/17.
 */
public class SomeKindaTest {

  @Test
  public void testSomething(){
// create config for proxy
    TcpProxyConfig config = new TcpProxyConfig(9200, "localhost", 9800);
    config.setWorkerCount(5);

// init proxy
    TcpProxy proxy = new TcpProxy(config);

// start proxy
    proxy.start();

// stop proxy
    proxy.shutdown();
  }
}
