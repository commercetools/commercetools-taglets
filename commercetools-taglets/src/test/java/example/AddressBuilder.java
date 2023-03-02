package example;

public interface AddressBuilder {
    static AddressBuilder of(CountryCode code) {
        return null;
    }
    static AddressBuilder of(Address address) {
        return null;
    }

    AddressBuilder company(String value);
    AddressBuilder firstName(String value);
    AddressBuilder lastName(String value);
    AddressBuilder streetName(String value);
    AddressBuilder streetNumber(String value);
    AddressBuilder postalCode(String value);
    AddressBuilder city(String value);
    Address build();
}
