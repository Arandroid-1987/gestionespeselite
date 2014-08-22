/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 *
 * @author Ronny
 */
public class MyAuthenticator extends Authenticator{
    private PasswordAuthentication pa;

    public MyAuthenticator(){
        pa = new PasswordAuthentication("arandroid.developers@gmail.com", "ara.1987");
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return pa;
    }
}
