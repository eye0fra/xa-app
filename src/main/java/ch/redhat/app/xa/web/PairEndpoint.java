package ch.redhat.app.xa.web;

import ch.redhat.app.xa.entity.Pair;
import ch.redhat.app.xa.service.PairService;
import com.wordnik.swagger.annotations.*;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Path("/pairs")
public class PairEndpoint {

    @Inject
    PairService pairService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() throws JMSException {
        List<Pair> pairs = new ArrayList<>();
        try {
            pairs = pairService.findAll();
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
        return Response.status(200).entity(pairs).build();
    }

    @GET
    @Path("/{key}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Response delete(@PathParam("key") String key) throws JMSException {
        Pair pair = null;
        try {
            pair = pairService.delete(key);
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
        return Response.status(200).entity(pair).build();
    }

    @GET
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Response deleteAll() throws JMSException {
        try {
            pairService.deleteAll();
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
        return findAll();
    }

}