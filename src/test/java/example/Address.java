package example;

import java.util.List;

public interface Address {
    Address withEmail(String value);
    Address withPhone(String value);
    List<String> getElements();

    public static List<String> of(Address address) {
        return address.getElements();
    }
}
