## viber-to-vcard

Extract contacts from a Viber datafile, to vCard format.

#### Usage
```
Usage: viber-to-vcard [-hV] [-o[=<outputPath>]] <viberDatafilePath>
Extract contacts from a Viber datafile, to vCard format
      <viberDatafilePath>    The path of the Viber datafile - usually resides in ${HOME}/.ViberPC/<yourPhoneNumber>/viber.db
  -h, --help                 Show this help message and exit.
  -o, --out[=<outputPath>]   The path of the output file, in vCard format
  -V, --version              Print version information and exit.
```

#### Example
```
mvn clean install
java -jar target/viber-to-vcard.jar -o ~/contacts.vcard ~/viber.db
```

#### Built / tested with
- Java 17
- Maven >= 3.6
