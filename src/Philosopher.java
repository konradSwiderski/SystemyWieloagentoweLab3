import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;


public class Philosopher extends Agent
{
    private int randInterval = 0;
    private Vector<AID> vectorIdOfKebabs = new Vector<>();
    private String leftFork;
    private String rightFork;
    private Vector<AID> vectorIdOfLeftForks = new Vector<>();
    private Vector<AID> vectorIdOfRightForks = new Vector<>();
    private int leftForkBusy;
    private int rightForkBusy;
    private int kony = 0;
    private int numberOfKebabs = 0;
    private int num = 0;
    private int answerFromKebab = 0;
    private int allowToGetFork = 0;

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
        while (vectorIdOfLeftForks.isEmpty()) { searchAgentLeftFork(); }
        while (vectorIdOfRightForks.isEmpty()) { searchAgentRightFork(); }

        System.out.print("))))))))))))MY LEFT" + vectorIdOfLeftForks.firstElement() + "))))))))))))))MY RIGHT" + vectorIdOfRightForks.firstElement());

        Behaviour creatingTokenBehaviour = new TickerBehaviour( this, 5000)
        {
            protected void onTick()
            {
                System.out.println("onTick_________________");
                if(allowToGetFork == 0) //I can
                {
                    allowToGetFork = 1;
                    if(!vectorIdOfKebabs.isEmpty())
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(vectorIdOfKebabs.firstElement());
                        myAgent.send(msg);
                        System.out.println("PHILOSPHER: I sent msg to Kebab");
                    }
                    //wait for answer from Kebab
                    addBehaviour(new CyclicBehaviour()
                    {
                        @Override
                        public void action()
                        {
                            System.out.println("Cyclic**********");
                            ACLMessage msg = myAgent.receive();
                            if (msg!= null)
                            {
                                if(msg.getPerformative() == ACLMessage.REQUEST)
                                {
                                    numberOfKebabs = Integer.parseInt(msg.getContent());
                                    System.out.println("PHILOSOPHER: Number of kebabs " + numberOfKebabs);

                                    if(numberOfKebabs > 0)
                                    {
                                        //send msg to fork
                                        //which one?
                                        int whichFork = ThreadLocalRandom.current().nextInt(1, 2 + 1);
                                        ACLMessage msgFork = new ACLMessage(ACLMessage.AGREE);
                                        System.out.println("whichFork" + whichFork);
                                        if (whichFork == 1)
                                            msgFork.addReceiver(vectorIdOfLeftForks.firstElement());
                                        else
                                            msgFork.addReceiver(vectorIdOfRightForks.firstElement());
//
                                        myAgent.send(msgFork);
//                                        System.out.println("PHILOSPHER: I sent msg to Fork");
                                        //wait for answer from Fork
                                        addBehaviour(new CyclicBehaviour() {
                                            @Override
                                            public void action()
                                            {
                                                ACLMessage msgFromFork = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
                                                if (msgFromFork!= null)
                                                {
                                                    int contentFork = Integer.parseInt(msgFromFork.getContent());
                                                    System.out.println("Witaj Forku!!!!!!!!!!!!!! " + contentFork);
                                                    if(contentFork == 0)
                                                        System.out.println("MOGE");
                                                    else
                                                        System.out.println("NIE MOGE");
                                                    myAgent.removeBehaviour(this);
                                                }
                                                else
                                                {   System.out.println("Brak");
                                                    block();
                                                }
                                            }
                                        });


                                    }
                                    myAgent.removeBehaviour(this);

                                }
                            }
                            else
                            {   System.out.println("Brak");
                                block();
                            }
                        }
                    });
                }
                else
                {

                }

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

    private void searchAgentLeftFork()
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentFork");
        sd.setName(leftFork);
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i = 0; i < result.length; ++i)
                vectorIdOfLeftForks.addElement(result[i].getName());
        }
        catch (FIPAException ex)
        {
            ex.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void searchAgentRightFork()
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agentFork");
        sd.setName(rightFork);
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i = 0; i < result.length; ++i)
                vectorIdOfRightForks.addElement(result[i].getName());
        }
        catch (FIPAException ex)
        {
            ex.printStackTrace();
        }
    }
}