import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PhilosopherCyclicBehaviour extends CyclicBehaviour
{
    private AID kebabServer;
    private int numberOfKebabs = 0;

    private int answerKebabCyclic = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setKebabServer(AID kebabServer) {
        this.kebabServer = kebabServer;
    }

    public AID getKebabServer() {
        return kebabServer;
    }

    public int getAnswerKebabCyclic() {
        return answerKebabCyclic;
    }

    public void setAnswerKebabCyclic(int answerKebabCyclic) {
        this.answerKebabCyclic = answerKebabCyclic;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void action()
    {
        ACLMessage msg = myAgent.receive();
        if (msg!= null)
        {
            if(msg.getPerformative() == ACLMessage.REQUEST)
            {

                numberOfKebabs = Integer.parseInt(msg.getContent());
                System.out.println("PHILOSOPHER: REQ Number of kebabs " + numberOfKebabs);
                if(numberOfKebabs>0)
                    answerKebabCyclic = 1;
                System.out.println("KEBAB sie odezwal " + answerKebabCyclic);
//                ACLMessage reply = msg.createReply();
//                reply.setPerformative(ACLMessage.REQUEST);
//                myAgent.send(reply);
            }
        }
        else
        {
            block();
        }

    }
}