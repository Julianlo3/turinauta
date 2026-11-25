package com.unicauca.turinauta.core.navigation

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.unicauca.turinauta.AppContainer
import com.unicauca.turinauta.TurinautaApplication
import com.unicauca.turinauta.feature.auth.AuthViewModel
import com.unicauca.turinauta.feature.collection.CollectionViewModel
import com.unicauca.turinauta.feature.explore.ExploreViewModel
import com.unicauca.turinauta.feature.explore.PlaceDetailViewModel
import com.unicauca.turinauta.feature.profile.ProfileViewModel
import com.unicauca.turinauta.feature.reports.ReportViewModel
import com.unicauca.turinauta.feature.routes.RoutesViewModel
import com.unicauca.turinauta.feature.visits.RegisterVisitUseCase

private fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as TurinautaApplication).container

/** Fábrica única de ViewModels con inyección manual desde AppContainer. */
val TurinautaViewModelFactory = viewModelFactory {
    initializer { ExploreViewModel(container().catalog, container().location) }
    initializer {
        val c = container()
        PlaceDetailViewModel(
            savedStateHandle = createSavedStateHandle(),
            catalog = c.catalog,
            routes = c.routes,
            auth = c.auth,
            visits = c.visits,
            location = c.location,
            network = c.network,
            registerVisit = RegisterVisitUseCase(c.visits),
        )
    }
    initializer { RoutesViewModel(container().routes) }
    initializer { AuthViewModel(createSavedStateHandle(), container().auth) }
    initializer { CollectionViewModel(container().auth, container().collections) }
    initializer { ProfileViewModel(container().auth, container().visits, container().collections, container().network, container().location) }
    initializer { ReportViewModel(container().reports) }
}
