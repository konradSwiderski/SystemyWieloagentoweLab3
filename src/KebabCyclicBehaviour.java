import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class KebabCyclicBehaviour extends CyclicBehaviour
{
    private int numberOfKebabs;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setNumberOfKebabs(int numberOfKebabs) {
        this.numberOfKebabs = numberOfKebabs;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void action()
    {
        ACLMessage msg = myAgent.receive();
        if (msg!= null)
        {
            if(msg.getPerformative() == ACLMessage.REQUEST)
            {
                System.out.println("KEBAB: I GOT");
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REQUEST);
                reply.setContent(Integer.toString(numberOfKebabs));
                myAgent.send(reply);
                System.out.println("KEBAB: I sent");
            }
        }
        else
        {
            block();
        }
    }
}