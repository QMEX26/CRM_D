package com.crm.config;

import com.crm.model.*;
import com.crm.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LeadRepository leadRepository;
    private final LeadAssignmentRepository leadAssignmentRepository;
    private final CallRepository callRepository;
    private final FollowUpRepository followUpRepository;
    private final NoteRepository noteRepository;
    private final SalesRepository salesRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.crm.service.FirebaseAuthService firebaseAuthService;

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Checking baseline system data initialization...");

        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        // 2. Initialize Users
        User admin = userRepository.findByEmail("admin@crm.com").orElseGet(() -> {
            logger.info("Seeding default Administrator: admin@crm.com / admin123");
            return userRepository.save(User.builder()
                    .name("System Administrator")
                    .email("admin@crm.com")
                    .phone("+91 98765 00001")
                    .password(passwordEncoder.encode("admin123"))
                    .role(adminRole)
                    .status("ACTIVE")
                    .build());
        });

        User agent = userRepository.findByEmail("agent@crm.com").orElseGet(() -> {
            logger.info("Seeding default Sales Agent: agent@crm.com / agent123");
            return userRepository.save(User.builder()
                    .name("Priya Sharma")
                    .email("agent@crm.com")
                    .phone("+91 98765 00002")
                    .password(passwordEncoder.encode("agent123"))
                    .role(userRole)
                    .status("ACTIVE")
                    .build());
        });

        User agent2 = userRepository.findByEmail("agent2@crm.com").orElseGet(() -> {
            logger.info("Seeding second Sales Agent: agent2@crm.com / agent123");
            return userRepository.save(User.builder()
                    .name("Kiran Rao")
                    .email("agent2@crm.com")
                    .phone("+91 98765 00003")
                    .password(passwordEncoder.encode("agent123"))
                    .role(userRole)
                    .status("ACTIVE")
                    .build());
        });

        // Ensure all existing users and admins have default shift assigned (10:00 AM – 07:00 PM)
        try {
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                if (u.getShift() == null) {
                    u.setShift(WorkShift.SHIFT_1000_1900);
                    userRepository.save(u);
                    logger.info("Updated shift for existing user {} ({}) to {}", u.getName(), u.getEmail(), WorkShift.SHIFT_1000_1900.getDisplayName());
                }
            }
        } catch (Exception e) {
            logger.warn("Could not backfill user shifts: {}", e.getMessage());
        }

        // Ensure default users are provisioned in Firebase Authentication
        try {
            String adminFbUid = firebaseAuthService.createFirebaseUser("admin@crm.com", "admin123", "System Administrator");
            if (adminFbUid != null && admin.getFirebaseUid() == null) {
                admin.setFirebaseUid(adminFbUid);
                userRepository.save(admin);
            }
            String agentFbUid = firebaseAuthService.createFirebaseUser("agent@crm.com", "agent123", "Priya Sharma");
            if (agentFbUid != null && agent.getFirebaseUid() == null) {
                agent.setFirebaseUid(agentFbUid);
                userRepository.save(agent);
            }
            String agent2FbUid = firebaseAuthService.createFirebaseUser("agent2@crm.com", "agent123", "Kiran Rao");
            if (agent2FbUid != null && agent2.getFirebaseUid() == null) {
                agent2.setFirebaseUid(agent2FbUid);
                userRepository.save(agent2);
            }
        } catch (Exception e) {
            logger.warn("Could not sync seed users to Firebase Authentication: {}", e.getMessage());
        }

        // 3. Initialize Sample Projects & Leads if empty
        if (projectRepository.count() == 0) {
            logger.info("Seeding sample projects and leads...");

            Project skyline = projectRepository.save(Project.builder()
                    .name("Skyline Towers")
                    .description("Premium 3 & 4 BHK luxury residences with panoramic views.")
                    .status("ACTIVE")
                    .build());

            Project greenValley = projectRepository.save(Project.builder()
                    .name("Green Valley Villas")
                    .description("Eco-friendly gated villa community with private clubhouses.")
                    .status("ACTIVE")
                    .build());

            projectRepository.save(Project.builder()
                    .name("CyberPark Commercial")
                    .description("Grade-A modern office spaces with high leasing yield.")
                    .status("ACTIVE")
                    .build());

            // Create Leads for Skyline Towers
            Lead lead1 = leadRepository.save(Lead.builder()
                    .project(skyline)
                    .name("Arun Kumar")
                    .phone("+91 98111 22334")
                    .email("arun.kumar@example.com")
                    .city("Bangalore")
                    .state("Karnataka")
                    .source("Website Inquiry")
                    .status("CONTACTED")
                    .businessOutcome("INTERESTED")
                    .additionalInfo("Looking for 3BHK East-facing unit on higher floor.")
                    .build());

            // First assignment to agent2, then reassigned to agent to demonstrate history!
            leadAssignmentRepository.save(LeadAssignment.builder()
                    .lead(lead1)
                    .user(agent2)
                    .assignedBy(admin)
                    .assignedAt(LocalDateTime.now().minusDays(5))
                    .unassignedAt(LocalDateTime.now().minusDays(2))
                    .isActive(false)
                    .build());

            leadAssignmentRepository.save(LeadAssignment.builder()
                    .lead(lead1)
                    .user(agent)
                    .assignedBy(admin)
                    .assignedAt(LocalDateTime.now().minusDays(2))
                    .isActive(true)
                    .build());

            // Call history on lead1
            callRepository.save(Call.builder()
                    .lead(lead1)
                    .user(agent2)
                    .startedAt(LocalDateTime.now().minusDays(4))
                    .endedAt(LocalDateTime.now().minusDays(4).plusMinutes(3))
                    .durationSeconds(180)
                    .callStatus("CONNECTED")
                    .businessOutcome("FOLLOW_UP")
                    .notes("Customer requested brochure and price sheet via WhatsApp.")
                    .build());

            callRepository.save(Call.builder()
                    .lead(lead1)
                    .user(agent)
                    .startedAt(LocalDateTime.now().minusDays(1))
                    .endedAt(LocalDateTime.now().minusDays(1).plusMinutes(8))
                    .durationSeconds(480)
                    .callStatus("CONNECTED")
                    .businessOutcome("INTERESTED")
                    .notes("Detailed discussion on unit 1402. Client interested in weekend site visit.")
                    .build());

            // Follow-up for lead1 (Today)
            followUpRepository.save(FollowUp.builder()
                    .lead(lead1)
                    .user(agent)
                    .scheduledTime(LocalDateTime.now().plusHours(2))
                    .status("PENDING")
                    .notes("Confirm weekend site visit timings with Arun.")
                    .build());

            // Note for lead1
            noteRepository.save(Note.builder()
                    .lead(lead1)
                    .user(agent)
                    .content("Client has budget of 1.8 Cr. Pre-approved home loan from HDFC Bank.")
                    .build());

            // Create Lead 2 (Overdue Follow-up)
            Lead lead2 = leadRepository.save(Lead.builder()
                    .project(skyline)
                    .name("Meera Nambiar")
                    .phone("+91 98222 33445")
                    .email("meera.n@example.com")
                    .city("Bangalore")
                    .state("Karnataka")
                    .source("Google Ads")
                    .status("FOLLOW_UP")
                    .businessOutcome("FOLLOW_UP")
                    .additionalInfo("Inquired about 4BHK Penthouse options.")
                    .build());

            leadAssignmentRepository.save(LeadAssignment.builder()
                    .lead(lead2)
                    .user(agent)
                    .assignedBy(admin)
                    .assignedAt(LocalDateTime.now().minusDays(3))
                    .isActive(true)
                    .build());

            followUpRepository.save(FollowUp.builder()
                    .lead(lead2)
                    .user(agent)
                    .scheduledTime(LocalDateTime.now().minusHours(4)) // Overdue!
                    .status("PENDING")
                    .notes("Urgent: Send customized penthouse floor plans.")
                    .build());

            // Create Lead 3 (Converted)
            Lead lead3 = leadRepository.save(Lead.builder()
                    .project(greenValley)
                    .name("Rajesh Gupta")
                    .phone("+91 98333 44556")
                    .email("rajesh.gupta@corp.com")
                    .city("Hyderabad")
                    .state("Telangana")
                    .source("Referral")
                    .status("CONVERTED")
                    .businessOutcome("CONVERTED")
                    .additionalInfo("Booked Villa #42 in Green Valley.")
                    .build());

            leadAssignmentRepository.save(LeadAssignment.builder()
                    .lead(lead3)
                    .user(agent)
                    .assignedBy(admin)
                    .assignedAt(LocalDateTime.now().minusDays(10))
                    .isActive(true)
                    .build());

            salesRepository.save(Sale.builder()
                    .lead(lead3)
                    .user(agent)
                    .dealValue(new BigDecimal("35000000.00")) // 3.5 Cr
                    .notes("Booking amount received via RTGS. Agreement drafted.")
                    .convertedAt(LocalDateTime.now().minusDays(1))
                    .build());

            // Create Lead 4 (Unassigned)
            leadRepository.save(Lead.builder()
                    .project(greenValley)
                    .name("Sunita Rao")
                    .phone("+91 98444 55667")
                    .email("sunita.rao@example.com")
                    .city("Hyderabad")
                    .state("Telangana")
                    .source("Social Media")
                    .status("NEW")
                    .businessOutcome(null)
                    .additionalInfo("Inbound lead from Facebook Campaign.")
                    .build());

            logger.info("Sample CRM data initialization finished successfully.");
        }
    }
}
