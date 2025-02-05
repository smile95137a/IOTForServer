//package com.mo.app.runner;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import com.mo.app.config.QuartzHandler;
//import com.mo.app.entity.job.ScheduledJob;
//import com.mo.app.entity.layout.Layout;
//import com.mo.app.entity.layout.LayoutArea;
//import com.mo.app.entity.user.Role;
//import com.mo.app.entity.user.User;
//import com.mo.app.enums.LayoutSize;
//import com.mo.app.enums.RoleName;
//import com.mo.app.repo.LayoutAreaRepository;
//import com.mo.app.repo.LayoutRepository;
//import com.mo.app.repo.MenuItemRepository;
//import com.mo.app.repo.RoleRepository;
//import com.mo.app.repo.ScheduledJobRepository;
//import com.mo.app.repo.UserRepository;
//import com.mo.app.utils.RandomUtils;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DatabaseInitializer implements CommandLineRunner {
//
//	private final PasswordEncoder passwordEncoder;
//
//	private final RoleRepository roleRepository;
//
//	private final UserRepository userRepository;
//
//	private final LayoutRepository layoutRepository;
//
//	private final LayoutAreaRepository layoutAreaRepository;
//
//	private final MenuItemRepository menuItemRepository;
//	private final MailService mailService;
//
//	private final ScheduledJobRepository scheduledJobRepository;
//
//	private final ScheduledJobService scheduledJobService;
//	private final QuartzHandler quartzHandler;
//
//	@Override
//	public void run(String... args) throws IOException {
//		if (userRepository.findAll().isEmpty()) {
//			createRoles();
//			createAdminUser();
//			createLayout();
//			createScheduledJobs();
//			log.info("Database initialized");
//		}
//		startMenuJobs();
//		log.info("MenuJob initialized");
//	}
//
//	private void createRoles() {
//		if (!roleRepository.existsByName(RoleName.ROLE_ADMIN)) {
//			Role adminRole = new Role();
//			adminRole.setName(RoleName.ROLE_ADMIN);
//			roleRepository.save(adminRole);
//		}
//
//		if (!roleRepository.existsByName(RoleName.ROLE_USER)) {
//			Role userRole = new Role();
//			userRole.setName(RoleName.ROLE_USER);
//			roleRepository.save(userRole);
//		}
//	}
//
//	private void createAdminUser() {
//		Set<Role> adminRoles = new HashSet<>();
//		Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
//				.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not found."));
//		adminRoles.add(adminRole);
//
//		User admin = new User();
//		admin.setUid(RandomUtils.genRandom(32, false));
//		admin.setUsername("admin");
//		admin.setPassword(passwordEncoder.encode("123456"));
//		admin.setName("ADMIN");
//		admin.setEmail("admin@gmail.com");
//		admin.setRoles(adminRoles);
//		admin.setCreateTime(LocalDateTime.now());
//		admin.setUpdateTime(LocalDateTime.now());
//		admin.setLastActiveTime(LocalDateTime.now());
//
//		userRepository.save(admin);
//	}
//
//	private void createLayout() {
//
//		LayoutArea area11 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(625).height(843)
//				.build();
//		LayoutArea area12 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(625).y(0).width(625).height(843)
//				.build();
//		LayoutArea area13 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(0).width(625).height(843)
//				.build();
//		LayoutArea area14 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1875).y(0).width(625).height(843)
//				.build();
//		LayoutArea area15 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(625).height(843)
//				.build();
//		LayoutArea area16 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(625).y(843).width(625).height(843)
//				.build();
//		LayoutArea area17 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(843).width(625).height(843)
//				.build();
//		LayoutArea area18 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1875).y(843).width(625).height(843)
//				.build();
//
//		List<LayoutArea> areas = Arrays.asList(area11, area12, area13, area14, area15, area16, area17, area18);
//
//		Layout layout = Layout.builder().name("大版型1").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL).areas(areas)
//				.createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout);
////
////		LayoutArea halfArea1 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1250).height(421)
////				.build();
////		LayoutArea halfArea2 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1251).y(0).width(1240).height(421)
////				.build();
////		LayoutArea halfArea3 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(421).width(2500).height(422)
////				.build();
////
////		List<LayoutArea> halfAreas = Arrays.asList(halfArea1, halfArea2, halfArea3);
////
////		Layout halfLayout = Layout.builder().name("小版型1").uid(RandomUtils.genRandom(32)).size(LayoutSize.HALF)
////				.areas(halfAreas).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
////
////		layoutRepository.save(halfLayout);
//
//		//
//		LayoutArea area21 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(833).height(687)
//				.build();
//		LayoutArea area22 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(0).width(833).height(687)
//				.build();
//		LayoutArea area23 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(0).width(833).height(687)
//				.build();
//		LayoutArea area24 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(687).width(833).height(687)
//				.build();
//		LayoutArea area25 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(687).width(833).height(687)
//				.build();
//		LayoutArea area26 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(687).width(833).height(687)
//				.build();
//		LayoutArea area27 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(1374).width(1250).height(312)
//				.build();
//		LayoutArea area28 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(1374).width(1250).height(312)
//				.build();
//
//		List<LayoutArea> areas22 = Arrays.asList(area21, area22, area23, area24, area25, area26, area27, area28);
//
//		Layout layout22 = Layout.builder().name("大版型2").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas22).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout22);
//
//		LayoutArea area31 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(2500).height(228)
//				.build();
//		LayoutArea area32 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(228).width(625).height(729)
//				.build();
//		LayoutArea area33 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(625).y(228).width(625).height(729)
//				.build();
//		LayoutArea area34 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(228).width(625).height(729)
//				.build();
//		LayoutArea area35 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1875).y(228).width(625).height(729)
//				.build();
//		LayoutArea area36 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(957).width(625).height(729)
//				.build();
//		LayoutArea area37 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(625).y(957).width(625).height(729)
//				.build();
//		LayoutArea area38 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(957).width(625).height(729)
//				.build();
//		LayoutArea area39 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1875).y(957).width(625).height(729)
//				.build();
//
//		List<LayoutArea> areas33 = Arrays.asList(area31, area32, area33, area34, area35, area36, area37, area38,
//				area39);
//
//		Layout layout33 = Layout.builder().name("版型3").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas33).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout33);
//
//		LayoutArea area41 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1667).height(844)
//				.build();
//		LayoutArea area42 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1667).y(0).width(833).height(422)
//				.build();
//		LayoutArea area43 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1667).y(422).width(833).height(422)
//				.build();
//		LayoutArea area44 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(844).width(833).height(843)
//				.build();
//		LayoutArea area45 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(844).width(833).height(843)
//				.build();
//		LayoutArea area46 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(844).width(833).height(843)
//				.build();
//
//		List<LayoutArea> areas44 = Arrays.asList(area41, area42, area43, area44, area45, area46);
//
//		Layout layout44 = Layout.builder().name("版型4").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas44).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout44);
//
//		LayoutArea area51 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1250).height(1686)
//				.build();
//		LayoutArea area52 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(0).width(1250).height(1686)
//				.build();
//
//		List<LayoutArea> areas55 = Arrays.asList(area51, area52);
//
//		Layout layout55 = Layout.builder().name("版型5").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas55).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout55);
//
//		LayoutArea area61 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1250).height(843)
//				.build();
//		LayoutArea area62 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(0).width(1250).height(843)
//				.build();
//		LayoutArea area63 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(1250).height(843)
//				.build();
//		LayoutArea area64 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(843).width(1250).height(843)
//				.build();
//
//		List<LayoutArea> areas66 = Arrays.asList(area61, area62, area63, area64);
//
//		Layout layout66 = Layout.builder().name("版型6").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas66).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout66);
//
//		LayoutArea area71 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(833).height(843)
//				.build();
//		LayoutArea area72 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(0).width(833).height(843)
//				.build();
//		LayoutArea area73 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(0).width(833).height(843)
//				.build();
//		LayoutArea area74 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(833).height(843)
//				.build();
//		LayoutArea area75 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(843).width(833).height(843)
//				.build();
//		LayoutArea area76 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(843).width(833).height(843)
//				.build();
//
//		List<LayoutArea> areas77 = Arrays.asList(area71, area72, area73, area74, area75, area76);
//
//		Layout layout77 = Layout.builder().name("版型7").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas77).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout77);
//
//		LayoutArea area81 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(833).height(843)
//				.build();
//		LayoutArea area82 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(0).width(833).height(843)
//				.build();
//		LayoutArea area83 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(0).width(833).height(562)
//				.build();
//		LayoutArea area84 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(562).width(833).height(562)
//				.build();
//		LayoutArea area85 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(833).height(843)
//				.build();
//		LayoutArea area86 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(843).width(833).height(843)
//				.build();
//		LayoutArea area87 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(1124).width(833).height(562)
//				.build();
//		List<LayoutArea> areas88 = Arrays.asList(area81, area82, area83, area84, area85, area86, area87);
//
//		Layout layout88 = Layout.builder().name("版型8").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas88).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout88);
//
//
//
//		LayoutArea area91 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(833).height(1052)
//				.build();
//		LayoutArea area92 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(833).y(0).width(833).height(1052)
//				.build();
//		LayoutArea area93 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1666).y(0).width(833).height(1052)
//				.build();
//		LayoutArea area94 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(1052).width(1250).height(634)
//				.build();
//		LayoutArea area95 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(1052).width(1250).height(634)
//				.build();
//		List<LayoutArea> areas99 = Arrays.asList(area91, area92, area93, area94, area95);
//
//		Layout layout99 = Layout.builder().name("版型9").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas99).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout99);
//
//	}
//
//	private void createScheduledJobs() {
//		ScheduledJob job1 = ScheduledJob.builder().uid(RandomUtils.genRandom(32)).jobName("MenuJob1").cronExpression("0 * * * * ?")
//				.beanClass("com.mo.app.jobs.MenuJob1").status("NONE").jobGroup("default").jobDataMap("")
//				.createTime(LocalDateTime.now()).createUserId(1L).updateTime(LocalDateTime.now()).updateUserId(1L)
//				.lastActiveTime(LocalDateTime.now()).remarks("MenuJob1").build();
//
//		scheduledJobRepository.save(job1);
//
//		ScheduledJob job2 = ScheduledJob.builder().uid(RandomUtils.genRandom(32)).jobName("MenuJob2").cronExpression("0 * * * * ?")
//				.beanClass("com.mo.app.jobs.MenuJob2").status("NONE").jobGroup("default").jobDataMap("")
//				.createTime(LocalDateTime.now()).createUserId(1L).updateTime(LocalDateTime.now()).updateUserId(1L)
//				.lastActiveTime(LocalDateTime.now()).remarks("MenuJob2").build();
//
//		scheduledJobRepository.save(job2);
//
//	}
//	private void startMenuJobs() {
//	    startMenuJob("MenuJob1");
//	    startMenuJob("MenuJob2");
//	}
//
//	private void startMenuJob(String jobName) {
//	    var queryJob = scheduledJobService.getScheduledJobByJobName(jobName);
//	    if (queryJob != null) {
//	        try {
//	            Class<?> clazz = Class.forName(queryJob.getBeanClass());
//	            quartzHandler.start(queryJob, clazz);
//	        } catch (ClassNotFoundException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	}
//
//}
