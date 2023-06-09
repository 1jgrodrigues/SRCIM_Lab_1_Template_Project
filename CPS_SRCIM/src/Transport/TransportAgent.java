package Transport;

import Product.ProductAgent;
import Utilities.DFInteraction;
import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (ITransport) instance;
            System.out.println(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Transport Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        // TO DO: Register in DF ***
        try {
            DFInteraction.RegisterInDF(this, "transport", "transport");
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        // TO DO: Add responder behaviour/s
        this.addBehaviour(new TransportAgent.responder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));

    }
    //Responder Request PA-TA
    private class responder extends AchieveREResponder {
        public responder(Agent a,MessageTemplate mt){
            super(a,mt);
        }
        @Override
        protected ACLMessage handleRequest (ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName() + ":Processing REQUEST message");
            ACLMessage msgREQ1=request.createReply();
            msgREQ1.setPerformative(ACLMessage.AGREE);
            //myLib.executeMove(origin,destination,id);

            return msgREQ1;
        }

        @Override
        protected ACLMessage prepareResultNotification (ACLMessage request, ACLMessage response) throws FailureException {
            System.out.println(myAgent.getLocalName() + ":Preparing result of REQUEST");
            ACLMessage msgREQ1=request.createReply();
            msgREQ1.setPerformative(ACLMessage.INFORM);
            return msgREQ1;
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
