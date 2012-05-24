package edu.bath.transitivityutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail.com
 */
class SerializationUtils {
    @SuppressWarnings("unchecked")
    static <T> T serializedCopy(T object) {
        try {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baout);

            out.writeObject(object);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baout.toByteArray()));
            return (T)in.readObject();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
