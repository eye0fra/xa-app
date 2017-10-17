package ch.redhat.app.xa.jms;


import ch.redhat.app.xa.entity.Pair;
import ch.redhat.app.xa.service.PairService;
import ch.redhat.app.xa.web.SwaggerJaxrsConfig;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.logging.Level;

@ResourceAdapter("remote-artemis")
@MessageDriven(name = "PairConsumer", activationConfig = {
//        Artemis convention
//        @ActivationConfigProperty(propertyName = "useJNDI", propertyValue = "false"),
//        @ActivationConfigProperty(propertyName = "destination", propertyValue = "galileo.endpoint")
        @ActivationConfigProperty(propertyName = "useJNDI", propertyValue = "true"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/testQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PairConsumer implements Serializable, MessageListener {

    private Logger LOGGER = LoggerFactory.getLogger(PairConsumer.class);

    @Resource
    MessageDrivenContext ctx;

    @Inject
    PairService pairService;

    /**
     * @see MessageListener#onMessage(Message)
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
        LOGGER.info("got message: " + message);
        TextMessage textMessage = (TextMessage) message;
        try {
            pairService.set(new Pair(message.getJMSMessageID(), textMessage.getText()));
            // It will rollback the first set and put the msg in DLQ
            //pairService.setWithRollback(new Pair(message.getJMSMessageID(), textMessage.getText()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Sample using Bean Management Transaction sample
     * Use: @TransactionManagement(value = TransactionManagementType.BEAN) on classe level
     * @param message
     */
    private void beanTransaction(Message message) {
        UserTransaction userTransaction = ctx.getUserTransaction();
        LOGGER.info("got message: " + message);
        TextMessage textMessage = (TextMessage) message;
        try {
            userTransaction.begin();
            pairService.set(new Pair(message.getJMSMessageID(), textMessage.getText()));
            //pairService.setWithRollback(new Pair(message.getJMSMessageID(), textMessage.getText()));
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                LOGGER.error(e1.getMessage());
            }
            throw new RuntimeException(e);
        }

    }
}
