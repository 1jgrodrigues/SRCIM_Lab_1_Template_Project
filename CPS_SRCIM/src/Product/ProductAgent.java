package Product;

import Utilities.DFInteraction;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.Vector;


/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation

    AID SelectedResource;

    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production

        //Initiator Contract Net PA-RA


        ACLMessage msgCNT=new ACLMessage(ACLMessage.CFP);
        try {
            DFAgentDescription[] dfAgentDescriptions = DFInteraction.SearchInDFByName(executionPlan.get(0),this);
            msgCNT.addReceiver(dfAgentDescriptions[0].getName());
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new ProductAgent.initiator(this,msgCNT));
/*
        //Initiator Request PA-TA
        ACLMessage msgREQ1=new ACLMessage(ACLMessage.REQUEST);
        try {
            DFAgentDescription[] transports = DFInteraction.SearchInDFByType("transport", this);
            msgREQ1.addReceiver(transports[0].getName());
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new ProductAgent.initiatorTA(this,msgREQ1));



        //Initiator Request PA-RA
        ACLMessage msgREQ2=new ACLMessage(ACLMessage.REQUEST);
        try {
            DFAgentDescription[] Skills = DFInteraction.SearchInDFByName(executionPlan.get(1),this);
            msgREQ2.addReceiver(Skills[1].getName());
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new ProductAgent.initiatorRA(this,msgREQ2));

         */

    }
    //Initiator Contract Net PA-RA
    private class initiator extends ContractNetInitiator {
        public initiator(Agent a, ACLMessage msgCNT){
            super(a,msgCNT);
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ":INFORM message received");
            //Initiator Request PA-TA
            ACLMessage msgREQ1=new ACLMessage(ACLMessage.REQUEST);
            try {
                DFAgentDescription[] transports = DFInteraction.SearchInDFByType("transport", this.getAgent());
                msgREQ1.addReceiver(transports[0].getName());//lista com ciclo for para todos os recivers
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }
            addBehaviour(new ProductAgent.initiatorTA(this.getAgent(),msgREQ1));

        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances){
            System.out.println(myAgent.getLocalName() + ":ALL PROPOSALS received");
            ACLMessage auxMsg=(ACLMessage) responses.get(0);
            SelectedResource = ((ACLMessage) responses.get(0)).getSender();
            ACLMessage reply=auxMsg.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(reply);
        }
    }


    //Initiator Request PA-TA
    public class initiatorTA extends AchieveREInitiator{
        public initiatorTA(Agent a,ACLMessage msgREQ1) {super(a,msgREQ1);}

        @Override
        protected void handleAgree(ACLMessage agree){
            System.out.println(myAgent.getLocalName() + ":AGREE message request");
        }
        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ":INFORM message received");
            //Initiator Request PA-RA
            ACLMessage msgREQ2=new ACLMessage(ACLMessage.REQUEST);
            msgREQ2.addReceiver(SelectedResource);
            addBehaviour(new ProductAgent.initiatorRA(myAgent,msgREQ2));
        }
    }


    //Initiator Request PA-RA
    public class initiatorRA extends AchieveREInitiator{
        public initiatorRA(Agent a,ACLMessage msgREQ2) {super(a,msgREQ2);}

        @Override
        protected void handleAgree(ACLMessage agree){
            System.out.println(myAgent.getLocalName() + ":AGREE message request");

        }
        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ":INFORM message received");
            ACLMessage msgCNT=new ACLMessage(ACLMessage.CFP);
            executionPlan.remove(0);
            if (executionPlan.size()>0) {
                //Initiator Contract Net PA-RA
                try {
                    DFAgentDescription[] dfAgentDescriptions = DFInteraction.SearchInDFByName(executionPlan.get(0), this.getAgent());
                    msgCNT.addReceiver(dfAgentDescriptions[0].getName());
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                addBehaviour(new ProductAgent.initiator(this.getAgent(), msgCNT));
            }
            else
                System.out.println("Terminei");

        }

    }


    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<String> getExecutionList(String productType){
        switch(productType){
            case "A": return Utilities.Constants.PROD_A;
            case "B": return Utilities.Constants.PROD_B;
            case "C": return Utilities.Constants.PROD_C;
        }
        return null;
    }
    
}
