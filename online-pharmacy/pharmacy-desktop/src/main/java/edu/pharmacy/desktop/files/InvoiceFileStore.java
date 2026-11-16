package edu.pharmacy.desktop.files;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/** Saves and loads invoices with a restrictive Java serialization filter. */
public final class InvoiceFileStore {
    private static final Set<String> ALLOWED_CLASSES = Set.of(
            Invoice.class.getName(),
            InvoiceLine.class.getName(),
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.time.Ser",
            "java.util.ArrayList",
            "java.lang.String");

    private InvoiceFileStore() {
    }

    public static void save(Invoice invoice, Path file) throws IOException {
        Path target = file.toAbsolutePath().normalize();
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (ObjectOutputStream output =
                     new ObjectOutputStream(Files.newOutputStream(target))) {
            output.writeObject(invoice);
        }
    }

    public static Invoice load(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input =
                     new ObjectInputStream(Files.newInputStream(
                             file.toAbsolutePath().normalize()))) {
            input.setObjectInputFilter(InvoiceFileStore::filter);
            Object value = input.readObject();
            if (!(value instanceof Invoice invoice)) {
                throw new IOException("The binary file does not contain an invoice.");
            }
            return invoice;
        }
    }

    private static ObjectInputFilter.Status filter(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > 20 || info.references() > 1_000
                || info.streamBytes() > 1_000_000) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> type = info.serialClass();
        if (type == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            return component.isPrimitive() || component == Object.class
                    ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
        }
        return ALLOWED_CLASSES.contains(type.getName())
                ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
    }
}
