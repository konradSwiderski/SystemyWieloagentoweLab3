import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class KebabCyclicBehaviour extends CyclicBehaviour
{
    private int numberOfKebabs = 0;

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
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REQUEST);
                reply.setContent(Integer.toString(numberOfKebabs));
                myAgent.send(reply);
            }
            if(msg.getPerformative() == ACLMessage.PROPOSE)
            {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                if(numberOfKebabs > 0)
                {
                    reply.setContent("1");
                    numberOfKebabs = numberOfKebabs - 1;
                }
                else
                    reply.setContent("0");
                myAgent.send(reply);
            }
        }
        else
        {
            block();
        }
    }
}