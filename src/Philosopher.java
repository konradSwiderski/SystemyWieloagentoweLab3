import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;


import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;


public class Philosopher extends Agent
{
    private int randInterval = 0;
    private Vector<AID> vectorIdOfKebabs = new Vector<>();
    private Vector<AID> vectorIdOfForks = new Vector<>();
    private String leftFork;
    private String rightFork;
    private int leftForkBusy;
    private int rightForkBusy;
    private int kony = 0;
    private int numberOfKebabs = 0;
    private int num = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Philosopher()
    {
        randInterval = ThreadLocalRandom.current().nextInt(500, 1500 + 1);
        System.out.println("_Time of interval behaviour: " + randInterval);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void setup()
    {
        super.setup();

        //Register agent
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentPhilosopher");
        sd.setName("Philosopher");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch(FIPAException ex)
        {
            ex.printStackTrace();
        }

        //Find correct names of left and right forks
        if(getLocalName().toString().equals("Philosopher1"))
        {
            leftFork = "Fork1";
            rightFork = "Fork5";
        }
        if(getLocalName().toString().equals("Philosopher2"))
        {
            leftFork = "Fork2";
            rightFork = "Fork1";
        }
        if(getLocalName().toString().equals("Philosopher3"))
        {
            leftFork = "Fork3";
            rightFork = "Fork2";
        }
        if(getLocalName().toString().equals("Philosopher4"))
        {
            leftFork = "Fork4";
            rightFork = "Fork3";
        }
        if(getLocalName().toString().equals("Philosopher5"))
        {
            leftFork = "Fork5";
            rightFork = "Fork4";
        }
        System.out.println("My name is: " + getLocalName() + " left " + leftFork + " right " + rightFork);

        while (vectorIdOfKebabs.isEmpty()) { searchServerOfKebab(); }
        while (vectorIdOfForks.isEmpty()) { searchServerOfForks(); }

//        ACLMessage reply = new ACLMessage(ACLMessage);
//        reply.addReceiver(client);
//        reply.setContent(msgStringBuilder.append(columnsStringBuilder).toString());
//        myAgent.send(reply);
        //ADD cyclic behaviour
        PhilosopherCyclicBehaviour philosopherCyclicBehaviour = new PhilosopherCyclicBehaviour();
        philosopherCyclicBehaviour.setKebabServer(vectorIdOfKebabs.firstElement());
        addBehaviour(philosopherCyclicBehaviour);




        Behaviour creatingTokenBehaviour = new TickerBehaviour( this, 2000)
        {
            protected void onTick()
            {
                ACLMessage recieve = myAgent.receive();
//                System.out.println(">>POPEK1");
                //chec wziecia widelca
                //Ping agentKebab
                if(!vectorIdOfKebabs.isEmpty())
                {
                    if(kony == 0)
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(vectorIdOfKebabs.firstElement());
                        myAgent.send(msg);
                        kony = 1;
//                        System.out.println(">>FIRST BLOCKl");
                        block();
                    }
                        //...
//                        System.out.println(">>POPEK2");
                        if(recieve != null)
                        {
//                            System.out.println(">>POPEK3");
                            if(recieve.getSender().toString().equals(vectorIdOfKebabs.firstElement().toString()))
                            {
                                numberOfKebabs = Integer.parseInt(recieve.getContent());
                                System.out.println("PHILOSOPHER: REQ Number of kebabs " + numberOfKebabs);
                                if(numberOfKebabs > 0)
                                {
                                    System.out.println("KEBAB > 0");
                                    ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
                                    msg.addReceiver(vectorIdOfForks.firstElement());
                                    myAgent.send(msg);
                                    block();
                                }
                            }
                            else if(recieve != null && recieve.getSender().toString().equals(vectorIdOfForks.firstElement().toString()))
                            {
                                leftForkBusy = Integer.parseInt(recieve.getContent());
                                System.out.println("PHILOSOPHER: AGREE leftForkBusy " + leftForkBusy);
                            }
                        }


                }
                num++;
                if(num>6) {
                    while (true);
                }
                System.out.println(">>POPEK4");
            }
        };
        addBehaviour(creatingTokenBehaviour);

        System.out.println("Philosopher has been started");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void searchServerOfKebab()
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentKebab");
        sd.setName("Kebab");
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i = 0; i < result.length; ++i)
                vectorIdOfKebabs.addElement(result[i].getName());
        }
        catch (FIPAException ex)
        {
            ex.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void searchServerOfForks()
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentFork");
        sd.setName("Fork");
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i = 0; i < result.length; ++i)
                vectorIdOfForks.addElement(result[i].getName());
        }
        catch (FIPAException ex)
        {
            ex.printStackTrace();
        }
    }
}