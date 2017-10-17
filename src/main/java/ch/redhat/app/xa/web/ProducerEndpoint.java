package ch.redhat.app.xa.web;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;

@Stateless
@Path("/producer")
public class ProducerEndpoint {

    @Resource(mappedName = "java:/JmsRemoteXA")
    private XAConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/testQueue")
    private Queue testQueue;

    private AtomicInteger count = new AtomicInteger();

    @GET
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response send() throws JMSException {
        XAConnection connection = null;
        TextMessage msg = null;
        try {
            connection = connectionFactory.createXAConnection();

            XASession session = connection.createXASession();

            MessageProducer producer = session.createProducer(testQueue);

            connection.start();

            msg = session.createTextMessage("This is a test message " + count.incrementAndGet());
            producer.send(msg);
            producer.close();

        } catch (JMSException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {

                }
            }
        }
        return Response.status(200).entity(msg.getText()).build();
    }

}