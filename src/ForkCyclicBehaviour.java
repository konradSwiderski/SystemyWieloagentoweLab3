import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ForkCyclicBehaviour extends CyclicBehaviour
{
    private int busy = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void action()
    {
        ACLMessage msg = myAgent.receive();
        if (msg!= null)
        {
            if(msg.getPerformative() == ACLMessage.AGREE)
            {
                System.out.println("FORK: AGREE " + busy + " " + myAgent.getName());
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent(Integer.toString(busy));
                myAgent.send(reply);
                busy = 1;
            }
            if(msg.getPerformative() == ACLMessage.CANCEL)
            {
                System.out.println("FORK: CANCEL " + myAgent.getName());
                busy = 0;
            }
        }
        else
        {
            block();
        }
    }
}