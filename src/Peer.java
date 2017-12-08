
/**
 *
 * @author itsgnegrao
 */
public class Peer {
	public static final int CPORT = 2363;
	public static final int SPORT = 2364;
	public static final String magic = "1234";
	public static String newUserStr =  "0";
	public static String newUserRepStr =      "1";
	public static String beaconStr =          "2";
	public static String beaconRepStr =       "3";
	public static String updateStr =          "4";
	public static String updateboth =         "5";
	public static String updateUsers =        "6";
	public static String updateMain =         "7";
	public static String allreadyUpdated =    "8";
	public static String sendUserStr =        "9";
	public static String sendMainStr =        "10";
	public static String fileReqStr =         "11";
	public static String YfileReqRepStr =     "12";
	public static String NfileReqRepStr =     "13";
	public static String chunkDowStr =        "14";
	public static String fileUploadStr =      "15";
	public static String newfileUploadStr =   "16";
	public static String newfileUploadRepStr= "17";
	public static String fileUploadRepStr =   "19";
	public static String fileComDeleteStr =   "20";
	public static String fileComDeleteRepStr= "21";
	public static String fileDeleteStr =      "22";
	public static String fileDeleteRepStr =   "23";
	public static String fileDownloadedAck =  "24";
	public static String sendMasterStr  =    "28";
        
        
	public static void main(String[] args) throws Exception {
		System.out.println("\n\n\n************ Bem Vindo ao P2P Torrent  ***************\n\n\n");
		Client client = new Client();
		Server server = new Server();
		client.start();
		server.start();
	}
    
}
