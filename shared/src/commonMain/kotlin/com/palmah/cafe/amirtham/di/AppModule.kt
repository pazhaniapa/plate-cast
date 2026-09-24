package com.palmah.cafe.amirtham.di


import com.palmah.cafe.amirtham.auth.repository.AuthRepository
import com.palmah.cafe.amirtham.auth.repository.FirebaseAuthRepository
import com.palmah.cafe.amirtham.auth.usecase.AuthUseCase
import com.palmah.cafe.amirtham.auth.viewmodel.AuthViewModel
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepository
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepositoryImpl
import com.palmah.cafe.amirtham.digitalSignage.useCase.DigitalSignageUseCase
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageDisplayViewModel
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageListViewModel
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageViewModel
import com.palmah.cafe.amirtham.home.viewmodel.SideMenuViewModel
import com.palmah.cafe.amirtham.menu.list.repository.FirebaseMenuRepository
import com.palmah.cafe.amirtham.menu.list.repository.MenuRepository
import com.palmah.cafe.amirtham.menu.list.usecase.MenuUseCase
import com.palmah.cafe.amirtham.menu.list.viewmodel.MenuViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<AuthRepository> { FirebaseAuthRepository() }

    single<UserRepository> { FirebaseUserRepository() }

    factory { AuthUseCase(get<AuthRepository>(), get<UserRepository>()) }

    viewModel { AuthViewModel(get()) }

    single<MenuRepository> { FirebaseMenuRepository() }

    single<DigitalSignageRepository> { DigitalSignageRepositoryImpl() }

    factory { MenuUseCase(get<MenuRepository>(), get<UserRepository>(), get<DigitalSignageRepository>()) }

    factory { DigitalSignageUseCase(get<DigitalSignageRepository>(), get<UserRepository>()) }

    viewModel { MenuViewModel(get(), get<DigitalSignageUseCase>(), get<UserRepository>()) }

    viewModel { DigitalSignageViewModel(get(), get<UserRepository>()) }

    viewModel { DigitalSignageDisplayViewModel(get()) }

    viewModel { DigitalSignageListViewModel(get()) }

    viewModel { SideMenuViewModel(get<UserRepository>()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}