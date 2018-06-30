package br.com.staroski.runtime;

/**
 * Used internally by the {@link Shell} class allowing it to enchain {@link ShellListener}s.
 * 
 * @author Ricardo Artur Staroski
 */
final class ShellListeners implements ShellListener {

    public static ShellListener add(ShellListener existingListener, ShellListener listenerToAdd) {
        return addInternal(existingListener, listenerToAdd);
    }

    public static ShellListener remove(ShellListener existingListener, ShellListener listenerToRemove) {
        return removeInternal(existingListener, listenerToRemove);
    }

    private static ShellListener addInternal(ShellListener existingListener, ShellListener listenerToAdd) {
        if (existingListener == null) {
            return listenerToAdd;
        }
        if (listenerToAdd == null) {
            return existingListener;
        }
        return new ShellListeners(existingListener, listenerToAdd);
    }

    private static ShellListener removeInternal(ShellListener existingListener, ShellListener listenerToRemove) {
        if (existingListener == listenerToRemove || existingListener == null) {
            return null;
        }
        if (existingListener instanceof ShellListeners) {
            ShellListeners tuple = (ShellListeners) existingListener;
            if (listenerToRemove == tuple.a) {
                return tuple.b;
            }
            if (listenerToRemove == tuple.b) {
                return tuple.a;
            }
            ShellListener a = removeInternal(tuple.a, listenerToRemove);
            ShellListener b = removeInternal(tuple.b, listenerToRemove);
            if (a == tuple.a && b == tuple.b) {
                return tuple;
            }
            return addInternal(a, b);
        }
        return existingListener;
    }

    private final ShellListener a;
    private final ShellListener b;

    private ShellListeners(ShellListener a, ShellListener b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void receive(String message) {
        a.receive(message);
        b.receive(message);
    }
}