package com.elvarg.net;

import java.math.BigInteger;

import io.netty.util.AttributeKey;


/**
 * A class containing different attributes
 * which affect networking in different ways.
 *
 * @author Swiffy
 */
public class NetworkConstants {

    /**
     * The game port
     */
    public static final int GAME_PORT = 43595;

    /**
     * The opcode for requesting a login.
     */
    public static final int LOGIN_REQUEST_OPCODE = 14;

    /**
     * Signifies a new connection.
     */
    public static final int NEW_CONNECTION_OPCODE = 16;

    /**
     * Signifies the return of an existing connection.
     */
    public static final int RECONNECTION_OPCODE = 18;

    /**
     * The time required for the channel to time out
     */
    public static final int SESSION_TIMEOUT = 15;

    /**
     * The keys used for encryption on login
     */
    public static final BigInteger RSA_MODULUS = new BigInteger("131409501542646890473421187351592645202876910715283031445708554322032707707649791604685616593680318619733794036379235220188001221437267862925531863675607742394687835827374685954437825783807190283337943749605737918856262761566146702087468587898515768996741636870321689974105378482179138088453912399137944888201");

    public static final BigInteger RSA_EXPONENT = new BigInteger("79304472214370922762932105237390187381463672363705375233978043425709379778525976284494572865658334707555904114207777777341892920168231399767577257735843278036440634354404060637137311110371217284157987350293683059890663583033195388794460636931915044283757261183264988297579912358758185856341914846035938600173");

    /**
     * The amount of connections that are allowed from the same host.
     */
    public static final int CONNECTION_LIMIT = 2;

    /**
     * The attribute that contains the key for a players session.
     */
    public static final AttributeKey<PlayerSession> SESSION_KEY = AttributeKey.valueOf("session.key");

    /**
     * The maximum amount of messages that can be processed in one sequence.
     */
    public static final int PACKET_PROCESS_LIMIT = 30;


}
