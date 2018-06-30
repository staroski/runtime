package br.com.staroski.runtime;
/**
 * This interface is a listener for the {@link Shell} class.<br>
 * It's used to receve the output and error messages from the {@link Shell}'s underlying process.
 * 
 * @see #receive(String)
 * @see Shell
 * 
 * @author Ricardo Artur Staroski
 */
public interface ShellListener {

    /**
     * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object</a> of this interface.
     */
    public static final ShellListener NULL = new ShellListener() {

        @Override
        public void receive(String text) { /* does nothing */ }
    };

    /**
     * Receives a message from the {@link Shell}'s underlying process.
     * 
     * @param message
     *            The received message..
     */
    public void receive(String message);
}