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
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;


public class Philosopher extends Agent
{
    private int randInterval = 0;
    private Vector<AID> vectorIdOfKebabs = new Vector<>();
    private Vector<AID> vectorIdOfLeftForks = new Vector<>();
    private Vector<AID> vectorIdOfRightForks = new Vector<>();
    private String leftFork = "";
    private String rightFork = "";
    private int iHaveLeftFork = 0;
    private int iHaveRightFork = 0;
    private int numberOfKebabsTable = 0;
    private int countOfMyKebabs = 0;
    private int state = 0;


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Philosopher()
    {
        randInterval = ThreadLocalRandom.current().nextInt(500, 1500 + 1);
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
        sd.setName(getLocalName());
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
        findCorrectNamesForks();

        //Search for agent Kebab and Forks
        while (vectorIdOfKebabs.isEmpty()) { searchServerOfKebab(); }
        while (vectorIdOfLeftForks.isEmpty()) { searchAgentLeftFork(); }
        while (vectorIdOfRightForks.isEmpty()) { searchAgentRightFork(); }

        //Main Behaviour
        Behaviour creatingTokenBehaviour = new TickerBehaviour( this, 5000)
        {
            protected void onTick()
            {
                System.out.println("______Current number of Kebabs: "+ countOfMyKebabs + " " + myAgent.getName());

                //STATE 0 - I DON'T HAVE ANY FORKS...
                if(state == 0)
                {
                    System.out.println("....State_0_#NO_FORKS_I'M_GETTING_FIRST_FORK...." + myAgent.getName());

                    //Ping kebab
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(vectorIdOfKebabs.firstElement());
                    myAgent.send(msg);

                    //Wait for answer from Kebab
                    addBehaviour(new CyclicBehaviour()
                    {
                        @Override
                        public void action()
                        {
                            ACLMessage msg = myAgent.receive();
                            if (msg!= null)
                            {
                                if(msg.getPerformative() == ACLMessage.REQUEST)
                                {
                                    numberOfKebabsTable = Integer.parseInt(msg.getContent());

                                    //Kebabs are on the table
                                    if(numberOfKebabsTable > 0)
                                    {
                                        //Send msg to Fork
                                        ACLMessage msgToFork = new ACLMessage(ACLMessage.AGREE);

                                        //Choose random fork
                                        int whichFork = ThreadLocalRandom.current().nextInt(1, 2 + 1);

                                        if (whichFork == 1)
                                            msgToFork.addReceiver(vectorIdOfLeftForks.firstElement());
                                        else
                                            msgToFork.addReceiver(vectorIdOfRightForks.firstElement());

                                        myAgent.send(msgToFork);

                                        //Wait for answer from Fork
                                        addBehaviour(new CyclicBehaviour() {
                                            @Override
                                            public void action()
                                            {
                                                ACLMessage msgFromFork = myAgent.receive();
                                                if (msgFromFork!= null)
                                                {
                                                    //Content #Busy or not
                                                    int contentFork = Integer.parseInt(msgFromFork.getContent());

                                                    if(whichFork == 1)
                                                    {
                                                        //Not busy
                                                        if(contentFork == 0)
                                                            iHaveLeftFork = 1; //Take fork
                                                        else
                                                            iHaveLeftFork = 0;
                                                    }
                                                    else
                                                    {
                                                        if(contentFork == 0)
                                                            iHaveRightFork = 1;
                                                        else
                                                            iHaveRightFork = 0;
                                                    }

                                                    System.out.println("LeftFork: " + iHaveLeftFork + " RightFork: "
                                                            + iHaveRightFork + " " + myAgent.getName());

                                                    //I don't have any....
                                                    if(iHaveLeftFork == 0 && iHaveRightFork == 0)
                                                        state = 0;
                                                    else
                                                        state = 1;

                                                    myAgent.removeBehaviour(this);
                                                }
                                                else
                                                {
                                                    block();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        //End of Philosopher
                                        System.out.println("::::::: THE END: My name: " + getLocalName() + " Time: " + randInterval
                                                + " I got kebabs: " + countOfMyKebabs);
                                        myAgent.doDelete();
                                    }
                                    myAgent.removeBehaviour(this);

                                }
                            }
                            else
                            {
                                block();
                            }
                        }
                    });
                }
                //STATE 1 - I HAVE ONE FORK
                else if(state == 1)
                {
                    System.out.println("....State_1_#ONE_FORK_I'M_GETTING_SECOND_FORK...." + myAgent.getName());

                    //Ping kebab
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(vectorIdOfKebabs.firstElement());
                    myAgent.send(msg);

                    //Wait for answer from Kebab
                    addBehaviour(new CyclicBehaviour()
                    {
                        @Override
                        public void action()
                        {
                            ACLMessage msg = myAgent.receive();
                            if (msg!= null)
                            {
                                if(msg.getPerformative() == ACLMessage.REQUEST)
                                {
                                    numberOfKebabsTable = Integer.parseInt(msg.getContent());

                                    //Kebabs are on the table
                                    if(numberOfKebabsTable > 0)
                                    {
                                        //Send msg to Fork
                                        ACLMessage msgFork = new ACLMessage(ACLMessage.AGREE);

                                        //Choose second fork
                                        if(iHaveLeftFork == 1)
                                            msgFork.addReceiver(vectorIdOfRightForks.firstElement());
                                        else if(iHaveRightFork == 1)
                                            msgFork.addReceiver(vectorIdOfLeftForks.firstElement());

                                        myAgent.send(msgFork);

                                        //Wait for answer from Fork
                                        addBehaviour(new CyclicBehaviour() {
                                            @Override
                                            public void action()
                                            {
                                                ACLMessage msgFromFork = myAgent.receive();
                                                if (msgFromFork!= null)
                                                {
                                                    //Content #Busy or not
                                                    int contentFork = Integer.parseInt(msgFromFork.getContent());

                                                    if(iHaveLeftFork == 1)
                                                    {
                                                        if(contentFork == 0)
                                                            iHaveRightFork = 1; //Take fork
                                                        else
                                                            iHaveRightFork = 0;
                                                    }
                                                    else
                                                    {
                                                        if(contentFork == 0)
                                                            iHaveLeftFork = 1;
                                                        else
                                                            iHaveLeftFork = 0;
                                                    }

                                                    System.out.println("LeftFork: " + iHaveLeftFork + " RightFork: "
                                                            + iHaveRightFork + " " + myAgent.getName());

                                                    //I have two forks?
                                                    if(iHaveLeftFork == 1 && iHaveRightFork == 1)
                                                        state = 2;
                                                    else
                                                    {
                                                        //Reset and put down forks
                                                        ACLMessage msgForkCancel = new ACLMessage(ACLMessage.CANCEL);
                                                        if(iHaveLeftFork == 1)
                                                            msgForkCancel.addReceiver(vectorIdOfLeftForks.firstElement());
                                                        else if(iHaveRightFork == 1)
                                                            msgForkCancel.addReceiver(vectorIdOfRightForks.firstElement());
                                                        myAgent.send(msgForkCancel);
                                                        iHaveLeftFork = 0;
                                                        iHaveRightFork = 0;

                                                        state = 0;
                                                    }
                                                    myAgent.removeBehaviour(this);
                                                }
                                                else
                                                {
                                                    block();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        //Put down fork
                                        ACLMessage msgForkCancel = new ACLMessage(ACLMessage.CANCEL);
                                        if(iHaveLeftFork == 1)
                                            msgForkCancel.addReceiver(vectorIdOfLeftForks.firstElement());
                                        else if(iHaveRightFork == 1)
                                            msgForkCancel.addReceiver(vectorIdOfRightForks.firstElement());
                                        iHaveLeftFork = 0;
                                        iHaveRightFork = 0;
                                        state = 0;
                                    }
                                    myAgent.removeBehaviour(this);
                                }
                            }
                            else
                            {
                                block();
                            }
                        }
                    });
                }
                //STATE 2 - I HAVE TWO FORKS | I'M GETTING KEBAB
                else if(state == 2)
                {

                    System.out.println("....State_2_#TWO_FORKS_I'M_GETTING_KEBAB...." + myAgent.getName());

                    //Get Kebab
                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    msg.addReceiver(vectorIdOfKebabs.firstElement());
                    myAgent.send(msg);

                    //Wait for answer from Kebab
                    addBehaviour(new CyclicBehaviour()
                    {
                        @Override
                        public void action()
                        {
                            ACLMessage msg = myAgent.receive();
                            if (msg!= null)
                            {
                                if(msg.getPerformative() == ACLMessage.PROPOSE)
                                {
                                    //Content #True or false
                                    int kebab = Integer.parseInt(msg.getContent());

                                    //Increment my kebabs
                                    if(kebab == 1)
                                        countOfMyKebabs = countOfMyKebabs + 1;
                                    state = 3;
                                    myAgent.removeBehaviour(this);
                                }
                            }
                            else
                            {
                                block();
                            }
                        }
                    });
                }
                //STATE 3 - RESET ALL
                else if(state == 3)
                {
                    System.out.println("....State_3_CANCELING_FORKS...." + myAgent.getName());

                    //Cancel left fork
                    ACLMessage msgLeftForkCancel = new ACLMessage(ACLMessage.CANCEL);
                    msgLeftForkCancel.addReceiver(vectorIdOfLeftForks.firstElement());
                    myAgent.send(msgLeftForkCancel);
                    iHaveLeftFork = 0;

                    //Cancel right fork
                    ACLMessage msgRightForkCancel = new ACLMessage(ACLMessage.CANCEL);
                    msgRightForkCancel.addReceiver(vectorIdOfRightForks.firstElement());
                    myAgent.send(msgRightForkCancel);
                    iHaveRightFork = 0;

                    state = 0;
                }
            }
        };
        addBehaviour(creatingTokenBehaviour);
        System.out.println("Philosopher has been started: " + getLocalName() + " left: " + leftFork + " right: " + rightFork);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void findCorrectNamesForks()
    {
        String myName = getLocalName().toString();

        if(myName.equals("Philosopher1"))
        {
            leftFork = "Fork1";
            rightFork = "Fork5";
        }
        else if(myName.equals("Philosopher2"))
        {
            leftFork = "Fork2";
            rightFork = "Fork1";
        }
        else if(myName.equals("Philosopher3"))
        {
            leftFork = "Fork3";
            rightFork = "Fork2";
        }
        else if(myName.equals("Philosopher4"))
        {
            leftFork = "Fork4";
            rightFork = "Fork3";
        }
        else if(myName.equals("Philosopher5"))
        {
            leftFork = "Fork5";
            rightFork = "Fork4";
        }
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