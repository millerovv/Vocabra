package com.example.alexmelnikov.vocabra.di;

import com.example.alexmelnikov.vocabra.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by AlexMelnikov on 25.02.18.
 */


@Singleton
@Component(modules = {
        NavigationModule.class,
})
public interface AppComponent {
    void inject (MainActivity mainActivity);
}
