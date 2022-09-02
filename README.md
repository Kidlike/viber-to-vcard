## viber-to-vcard

Extract contacts from a Viber datafile, to vCard format.

Why? because I lost my contacts 😱,
and I figured out that Viber Desktop stores all your contacts on disk 😎,
even if it doesn't show them in the UI 🧐

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
