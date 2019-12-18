package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        
        return gameai(currentBoard);
    }
    public int gameai(GameState game)
    {
        int LastMove = 0;
        int EndState = Integer.MIN_VALUE;
        int BestMove = 0;
        int depth = 0;
        long TimeLimit = System.currentTimeMillis() + (5000);
        for(int i=0;i<=Integer.MAX_VALUE;i++)
        {
            long now = System.currentTimeMillis();
            if(now>=TimeLimit)
            {
                System.out.println("break");
                break;
            }
        for(int x=1;x<7;x++)
        {
            now = System.currentTimeMillis();
            if(now>=TimeLimit)
            {
                 System.out.println("break");
                break;
            }
            System.out.println(x);
            if(game.moveIsPossible(x))
            {
                GameState gamestate = game.clone();
                if(gamestate.makeMove(x))
                {
                    BestMove = miniMax(gamestate,depth,1,TimeLimit);
                            
                }
                else
                {
                    BestMove = miniMax(gamestate,depth,0,TimeLimit);
                }
                //setting a move which will result in best outcome 
            }
            if(BestMove>EndState)
            {
                EndState = BestMove;
                LastMove= x;
            }
        }
        depth++;
        }
        return LastMove; // to bring out the best move
    }
    public int miniMax(GameState gameBoard,int depth,int playerTurn,long TimeLimit)
    {
        int EndState;
        if(playerTurn==0)
        {
            EndState = Integer.MAX_VALUE;
            
        }
        
        else
        {
            EndState = Integer.MIN_VALUE;
        }
        int BestMove;
        if(depth<=0)
        {
             return getScore(gameBoard);
        }
           
        int oneMoreChance = 0; 
        //itterating all the moves
        for(int i=1;i<7;i++)
        {
            if(gameBoard.moveIsPossible(i))
            {
                GameState gameState= gameBoard.clone();
                //condition to check the last pebble going into the house for extra chance
                if(gameState.makeMove(i))
                {
                    oneMoreChance = 1;
                }
                else{
                    oneMoreChance = 0 ;
                }
                if(TimeLimit > System.currentTimeMillis())
                {
                    if(playerTurn==1)
                    {
                        //recursive reducing the depth after each move
                        if(oneMoreChance==1)
                        {
                            BestMove = miniMax(gameState, depth-1, 1,TimeLimit);
                        }
                        else{
                            BestMove = miniMax(gameState, depth-1, 0,TimeLimit);
                        }
                        EndState = Math.max(EndState, BestMove);
                    }
                    else
                    {
                        if(oneMoreChance==1){
                            BestMove = miniMax(gameState, depth-1, 0,TimeLimit);
                        }
                        else{
                            BestMove = miniMax(gameState, depth-1, 1,TimeLimit);
                        }
                        EndState = Math.min(EndState, BestMove);
                    }
                }
            }
        }
        return EndState;
    }
    // Function to measure how gooad a move is for ai  
    public int getScore(GameState game)
    {
        return game.getScore(player) - game.getScore(SwitchPlayer());
    }
    //used to get next player turn
    private int SwitchPlayer(){
        if(player==1)
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
//    public int getRandom()
//    {
//        return 1 + (int)(Math.random() * 6);
//    }
}