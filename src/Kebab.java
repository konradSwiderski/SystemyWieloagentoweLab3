import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;


public class Kebab extends Agent
{
    private int numberOfKebabs = 15;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void setup()
    {
        super.setup();

        //Register agent
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentKebab");
        sd.setName("Kebab");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch(FIPAException ex)
        {
            ex.printStackTrace();
        }

        //ADD CyclicBehaviour
        KebabCyclicBehaviour kebabCyclicBehaviour = new KebabCyclicBehaviour();
        kebabCyclicBehaviour.setNumberOfKebabs(numberOfKebabs);
        addBehaviour(kebabCyclicBehaviour);
    }
}