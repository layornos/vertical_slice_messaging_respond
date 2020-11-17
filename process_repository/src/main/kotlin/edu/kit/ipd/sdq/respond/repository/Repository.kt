package edu.kit.ipd.sdq.respond.repository

import edu.kit.ipd.sdq.respond.repository.tables.Plant
import edu.kit.ipd.sdq.respond.repository.tables.Process
import javax.persistence.EntityManagerFactory

typealias ProcessId = Int
typealias ProcessContent = String

interface Repository {
    fun registerProcess(process: Process): Process
    fun getProcess(processId: ProcessId, plant: Plant): Process?
    fun removeProcess(processId: ProcessId, plant: Plant)
    fun getProcesses(plant: Plant): List<ProcessDescriptor>
    fun removeAllProcesses(plant: Plant)
    fun updateProcess(processId: ProcessId, plant: Plant, processContent: ProcessContent): Process?
    fun getPlants(): List<Plant>
    fun getPlant(plantPath: String): Plant?
}

class HibernateRepository(private val sessionFactory: EntityManagerFactory) : Repository {

    override fun registerProcess(process: Process): Process {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        entityManager.persist(process)
        entityManager.transaction.commit()
        entityManager.close()
        return process
    }

    override fun getProcess(processId: ProcessId, plant: Plant): Process? {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("from Process  where plant_id = :plant and id = :id", Process::class.java)
        query.setParameter("plant", plant.id)
        query.setParameter("id", processId)
        query.executeUpdate()
        return query.resultList.firstOrNull()
    }

    override fun removeProcess(processId: ProcessId, plant: Plant) {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        val query = entityManager.createQuery("delete from Process where plant_id = :plant and id = :id")
        query.setParameter("plant", plant.id)
        query.setParameter("id", processId)
        query.executeUpdate()
        entityManager.close()
    }

    override fun getProcesses(plant: Plant): List<ProcessDescriptor> {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("from Process where plant_id = :plant", Process::class.java)
        query.setParameter("plant", plant.id)
        return query.resultList.map { it.processDescriptor }
    }

    override fun removeAllProcesses(plant: Plant) {
        val entityManager = sessionFactory.createEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        entityManager.transaction.begin()
        val query = entityManager.createQuery("delete from Process where plant_id = :plant")
        query.setParameter("plant", plant.id)
        query.executeUpdate()
        entityManager.close()
    }

    override fun updateProcess(processId: ProcessId, plant: Plant, processContent: ProcessContent): Process? {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        val process = getProcess(processId, plant) ?: return null
        process.source = processContent
        entityManager.transaction.commit()
        entityManager.close()
        return process
    }

    override fun getPlant(plantPath: String): Plant? {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("select p from Plant p where path = :path", Plant::class.java)
        query.setParameter("path", plantPath)
        return query.resultList.firstOrNull()
    }

    override fun getPlants(): List<Plant> {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("from Plant", Plant::class.java)
        return query.resultList

    }

    fun addPlant(plantPath: String): Plant {
        val plant = getPlant(plantPath)
        if (plant != null) {
            return plant
        }

        val newPlant = Plant(plantPath)
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        entityManager.persist(newPlant)
        entityManager.transaction.commit()
        entityManager.close()
        return newPlant
    }
}

class ProcessDescriptor(val name: String, val id: Int)

val Process.processDescriptor: ProcessDescriptor
    get() = ProcessDescriptor(this.name, this.id)

