package Resource;

import Transport.TransportAgent;
import Utilities.DFInteraction;
import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ResourceAgent extends Agent {

    String id;
    IResource myLib;
    String description;
    String[] associatedSkills;
    String location;

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
            myLib = (IResource) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.location = (String) args[3];

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        //TO DO: Register in DF with the corresponding skills as services ***
        try {
            DFInteraction.RegisterInDF(this, associatedSkills, "Skills");
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        // TO DO: Add responder behaviour/s
        this.addBehaviour(new ResourceAgent.responderRA(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        this.addBehaviour(new ResourceAgent.responderCNT(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));

    }
    //Responder Request PA-RA
    private class responderRA extends AchieveREResponder {
        public responderRA(Agent a, MessageTemplate mt){
            super(a,mt);
        }
        @Override
        protected ACLMessage handleRequest (ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName() + ":Processing REQUEST message");
            ACLMessage msg=request.createReply();
            msg.setPerformative(ACLMessage.AGREE);
            myLib.executeSkill(associatedSkills[0]);
            return msg;
        }

        @Override
        protected ACLMessage prepareResultNotification (ACLMessage request, ACLMessage response) throws FailureException {
            System.out.println(myAgent.getLocalName() + ":Preparing result of REQUEST");
            ACLMessage msg=request.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            return msg;
        }
    }
    //Responder Contract Net PA-RA
    private class responderCNT extends ContractNetResponder {
        public responderCNT(Agent a,MessageTemplate mt){
            super(a,mt);
        }
        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,FailureException,NotUnderstoodException{
            System.out.println(myAgent.getLocalName() + ":Processing CFP message");
            ACLMessage msgCNT=cfp.createReply();
            msgCNT.setPerformative(ACLMessage.PROPOSE);
            msgCNT.setContent("My Proposal value");
            return msgCNT;
        }

        @Override
        protected ACLMessage handleAcceptProposal (ACLMessage cfp,ACLMessage propose, ACLMessage accept) throws FailureException{
            System.out.println(myAgent.getLocalName() + ":Preparing result of CFP");
            ACLMessage msgCNT=cfp.createReply();
            msgCNT.setPerformative(ACLMessage.INFORM);
            return msgCNT;
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown(); 
    }
}
