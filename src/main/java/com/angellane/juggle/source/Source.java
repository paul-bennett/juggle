package com.angellane.juggle.source;

import com.angellane.juggle.Juggler;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Source {
    private WeakReference<Juggler> juggler;
    public void setJuggler(Juggler juggler) { this.juggler = new WeakReference<>(juggler); }
    public Juggler getJuggler() { return juggler.get(); }

    public abstract Optional<URL> configure();

    public abstract Stream<Class<?>> classStream();
}
