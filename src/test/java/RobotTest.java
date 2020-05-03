import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;
import com.google.maps.model.EncodedPolyline;
import service.RobotMovementService;

public class RobotTest {

    @Test
    public void poylineMustBeDecoded(){

       // String polyline = "orl~Ff|{uO~@y@";

        EncodedPolyline encoder = Mockito.mock(EncodedPolyline.class);
        RobotMovementService robotMovementService = new RobotMovementService(encoder);

        robotMovementService.decode();

        verify(encoder).decodePath();
    }
}
