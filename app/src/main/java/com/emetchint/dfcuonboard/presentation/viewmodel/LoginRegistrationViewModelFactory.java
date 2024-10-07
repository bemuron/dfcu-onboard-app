package com.emetchint.dfcuonboard.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.emetchint.dfcuonboard.data.DFCUOnboardRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link DFCUOnboardRepository}
 */
public class LoginRegistrationViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private final DFCUOnboardRepository mRepository;

  public LoginRegistrationViewModelFactory(DFCUOnboardRepository repository) {
    this.mRepository = repository;
  }

  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    //noinspection unchecked
    return (T) new LoginRegisterActivityViewModel(mRepository);
  }
}
