package ir.co.contact.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.co.contact.data.repositories.ContactRepositoryImpl
import ir.co.contact.domain.repositories.ContactRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(contactRepositoryImpl: ContactRepositoryImpl): ContactRepository

}