package example;

public class AddressExample {
    private Address address;

    public void byCountryCreation() {
        Address commercetoolsBerlin = AddressBuilder.of(CountryCode.DE)
                .company("commercetools")
                .firstName("John").lastName("Doe")
                .streetName("Schuhmannstraße").streetNumber("17")
                .postalCode("10117").city("Berlin")
                .build();
    }

    public void buildByExistingAddress() {
        Address commercetoolsMunich = AddressBuilder.of(address)
                .streetName("Ganghoferstraße").streetNumber("68b")
                .postalCode("80339").city("München")
                .build();
    }

    public void withMethods() {
        final Address addressWithContactData = address
                .withEmail("hello@commercetools.de")
                .withPhone("+49.89.99 82 996-0");
    }
}
