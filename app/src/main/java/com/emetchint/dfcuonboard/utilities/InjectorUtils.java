/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emetchint.dfcuonboard.utilities;

import android.content.Context;

import com.emetchint.dfcuonboard.data.DFCUOnboardRepository;
import com.emetchint.dfcuonboard.data.database.OnboardDatabase;
import com.emetchint.dfcuonboard.data.network.LoginUser;
import com.emetchint.dfcuonboard.utilities.AppExecutors;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;

import java.util.Date;

/**
 * Provides static methods to inject the various classes needed for Sunshine i.e
 * The purpose of InjectorUtils is to provide static methods for dependency injection.
 * Dependency injection is the idea that you should make required components available for a class,
 * instead of creating them within the class itself.
 */
public class InjectorUtils {

    public static DFCUOnboardRepository provideRepository(Context context) {
        OnboardDatabase database = OnboardDatabase.getInstance(context.getApplicationContext());

        AppExecutors executors = AppExecutors.getInstance();

        LoginUser loginUser =
                LoginUser.getInstance(context.getApplicationContext(), executors);

        return DFCUOnboardRepository.getInstance(database.usersDao(),
                loginUser, executors);
    }

    public static LoginUser provideLoginUser(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return LoginUser.getInstance(context.getApplicationContext(), executors);
    }

    public static LoginRegistrationViewModelFactory provideLoginRegistrationViewModelFactory(Context context) {
        DFCUOnboardRepository repository = provideRepository(context.getApplicationContext());
        return new LoginRegistrationViewModelFactory(repository);
    }

    /*
     * Dependency injection is the idea that you should make required components available
     * for a class, instead of creating them within the class itself. An example of how the
     * Sunshine code does this is that instead of constructing the WeatherNetworkDatasource
     * within the SunshineRepository, the WeatherNetworkDatasource is created via InjectorUtilis
     * and passed into the SunshineRepository constructor. One of the benefits of this is that
     * components are easier to replace when you're testing. You can learn more about dependency
     * injection here. For now, know that the methods in InjectorUtils create the classes you
     * need, so they can be passed into constructors.
     * */
}