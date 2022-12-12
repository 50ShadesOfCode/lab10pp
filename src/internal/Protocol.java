//SG$SID$ @(#)Protocol.java	Tue Dec 01 15:35:07 2015

package internal;

/**
 * <p>CMD interface: Client message IDs 
 * @author Sergey Gutnikov
 * @version 1.0
 */
interface CMD {
// message IDs:
	byte 	CMD_CONTEXT			= 1,
			CMD_CONNECT 		= 2,
			CMD_DISCONNECT		= 3,
			CMD_USER 			= 4,
			CMD_CHECK_MAIL		= 5,
			CMD_LETTER			= 6,
			CMD_IMAGE = 7,
			CMD_CHECK_FILES = 8;
}

/**
 * <p>RESULT interface: Result codes 
 * @author Sergey Gutnikov
 * @version 1.0
 */
interface RESULT {
// Result codes:
	int 	RESULT_CODE_OK 		= 0,
			RESULT_CODE_ERROR 	= -1;
}	

/**
 * <p>PORT interface: Port # 
 * @author Sergey Gutnikov
 * @version 1.0
 */
interface PORT {
// Port #
	int 	PORT 				= 8071;
}

/**
 * <p>Protocol class: Protocol constants 
 * @author Sergey Gutnikov
 * @version 1.0
 */
public class Protocol implements CMD, RESULT, PORT {
	public static final byte 	CMD_MIN = CMD_CONTEXT,
								CMD_MAX = CMD_LETTER;
	public static boolean validID( byte id ) {
		return id >= CMD_MIN && id <= CMD_MAX; 
	}
}

